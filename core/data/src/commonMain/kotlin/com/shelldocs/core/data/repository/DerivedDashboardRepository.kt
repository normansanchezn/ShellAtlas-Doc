package com.shelldocs.core.data.repository

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.getOrDefault
import com.shelldocs.core.common.result.map
import com.shelldocs.core.data.demo.DemoActivityFeed
import com.shelldocs.core.domain.entity.dashboard.DashboardMetrics
import com.shelldocs.core.domain.entity.dashboard.ModuleCoverage
import com.shelldocs.core.domain.entity.dashboard.StatusBreakdown
import com.shelldocs.core.domain.entity.dashboard.UsagePoint
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.DocumentStatus
import com.shelldocs.core.domain.entity.onboarding.KnowledgeProgress
import com.shelldocs.core.domain.repository.DashboardRepository
import com.shelldocs.core.domain.repository.DocumentRepository
import com.shelldocs.core.domain.repository.KnowledgeCheckpointRepository
import com.shelldocs.core.domain.usecase.assistant.EvaluateDocumentHealthUseCase

/**
 * Builds the dashboard from the live document corpus: counts, status donut,
 * per-module coverage and knowledge health are all computed, not hardcoded.
 */
class DerivedDashboardRepository(
    private val documentRepository: DocumentRepository,
    private val evaluateHealth: EvaluateDocumentHealthUseCase,
    private val knowledgeCheckpointRepository: KnowledgeCheckpointRepository,
) : DashboardRepository {

    override suspend fun metrics(): DomainResult<DashboardMetrics> {
        val progress = knowledgeCheckpointRepository.progress().getOrDefault(KnowledgeProgress(0, 0))
        return documentRepository.documents().map { documents -> buildMetrics(documents, progress) }
    }

    private fun buildMetrics(documents: List<Document>, knowledgeProgress: KnowledgeProgress): DashboardMetrics {
        val total = documents.size
        val outdated = documents.count { it.status == DocumentStatus.OUTDATED }
        val healthScores = documents.map { evaluateHealth(it).score }
        val knowledgeHealth = healthScores.average().toIntOr(0)
        val healthyCount = healthScores.count { it >= 70 }

        return DashboardMetrics(
            totalDocuments = total,
            totalDocumentsDelta = DemoActivityFeed.DOCS_DELTA_THIS_WEEK,
            outdatedDocuments = outdated,
            outdatedDocumentsDelta = DemoActivityFeed.OUTDATED_DELTA_THIS_WEEK,
            coverageScorePercent = percent(healthyCount, total),
            aiQueriesThisWeek = DemoActivityFeed.AI_QUERIES_THIS_WEEK,
            aiQueriesDeltaPercent = DemoActivityFeed.AI_QUERIES_DELTA_PERCENT,
            knowledgeHealthScore = knowledgeHealth,
            docsReviewedPercent = percent(healthyCount, total),
            sourcesSynced = DemoActivityFeed.SOURCES_SYNCED,
            sourcesTotal = DemoActivityFeed.SOURCES_TOTAL,
            aiAccuracyPercent = DemoActivityFeed.AI_ACCURACY_PERCENT,
            staleRatePercent = percent(outdated, total),
            moduleCoverage = moduleCoverage(documents),
            statusBreakdown = statusBreakdown(documents),
            usage = DemoActivityFeed.weeklyUsage,
            recentActivity = DemoActivityFeed.recentActivity,
            attentionItems = DemoActivityFeed.attentionItems,
            knowledgeCheckpointsCompleted = knowledgeProgress.completed,
            knowledgeCheckpointsTotal = knowledgeProgress.total,
            projectKnowledgeScorePercent = knowledgeProgress.percent,
        )
    }

    private fun moduleCoverage(documents: List<Document>): List<ModuleCoverage> =
        documents.groupBy { it.attributes.module.ifBlank { "General" } }
            .map { (module, docs) ->
                ModuleCoverage(
                    module = module,
                    coveragePercent = docs.map { evaluateHealth(it).score }.average().toIntOr(0),
                )
            }
            .sortedBy { it.module.lowercase() }

    private fun statusBreakdown(documents: List<Document>): StatusBreakdown {
        val total = documents.size
        return StatusBreakdown(
            publishedPercent = percent(documents.count { it.status == DocumentStatus.PUBLISHED }, total),
            outdatedPercent = percent(documents.count { it.status == DocumentStatus.OUTDATED }, total),
            draftPercent = percent(documents.count { it.status == DocumentStatus.DRAFT }, total),
            pendingPercent = percent(documents.count { it.status == DocumentStatus.UPDATES_PENDING }, total),
        )
    }

    private fun percent(part: Int, total: Int): Int =
        if (total == 0) 0 else (part * 100) / total

    private fun Double.toIntOr(fallback: Int): Int = if (isNaN()) fallback else toInt()
}
