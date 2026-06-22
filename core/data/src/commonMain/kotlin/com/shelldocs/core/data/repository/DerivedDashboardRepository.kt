package com.shelldocs.core.data.repository

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.getOrDefault
import com.shelldocs.core.common.result.map
import com.shelldocs.core.domain.entity.assistant.MessageRole
import com.shelldocs.core.domain.entity.dashboard.AreaCoverage
import com.shelldocs.core.domain.entity.dashboard.DashboardMetrics
import com.shelldocs.core.domain.entity.dashboard.OwnerStat
import com.shelldocs.core.domain.entity.dashboard.StatusCount
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.onboarding.KnowledgeProgress
import com.shelldocs.core.domain.repository.ConversationRepository
import com.shelldocs.core.domain.repository.DashboardRepository
import com.shelldocs.core.domain.repository.DocumentRepository
import com.shelldocs.core.domain.repository.KnowledgeCheckpointRepository
import com.shelldocs.core.domain.usecase.assistant.EvaluateDocumentHealthUseCase

/**
 * Builds the dashboard from the live document corpus and checkpoint/conversation
 * state — every figure is computed from existing data, nothing is hardcoded.
 */
class DerivedDashboardRepository(
    private val documentRepository: DocumentRepository,
    private val evaluateHealth: EvaluateDocumentHealthUseCase,
    private val knowledgeCheckpointRepository: KnowledgeCheckpointRepository,
    private val conversationRepository: ConversationRepository,
) : DashboardRepository {

    override suspend fun metrics(): DomainResult<DashboardMetrics> {
        val progress = knowledgeCheckpointRepository.progress().getOrDefault(KnowledgeProgress(0, 0))
        val aiUsageCount = conversationRepository.conversations().getOrDefault(emptyList())
            .sumOf { conversation -> conversation.messages.count { it.role == MessageRole.USER } }
        return documentRepository.documents().map { documents -> buildMetrics(documents, progress, aiUsageCount) }
    }

    private fun buildMetrics(
        documents: List<Document>,
        knowledgeProgress: KnowledgeProgress,
        aiUsageCount: Int,
    ): DashboardMetrics {
        val healthyCount = documents.count { evaluateHealth(it).isHealthy }

        return DashboardMetrics(
            knowledgeTransferCompleted = knowledgeProgress.completed,
            knowledgeTransferTotal = knowledgeProgress.total,
            knowledgeTransferPercent = knowledgeProgress.percent,
            healthyDocuments = healthyCount,
            attentionDocuments = documents.size - healthyCount,
            areaCoverage = areaCoverage(documents),
            statusBreakdown = statusBreakdown(documents),
            aiUsageCount = aiUsageCount,
            topOwners = topOwners(documents),
        )
    }

    private fun areaCoverage(documents: List<Document>): List<AreaCoverage> =
        documents.groupBy { it.attributes.area?.displayName ?: "Unassigned" }
            .map { (area, docs) ->
                AreaCoverage(
                    area = area,
                    documentCount = docs.size,
                    healthyPercent = percent(docs.count { evaluateHealth(it).isHealthy }, docs.size),
                )
            }
            .sortedByDescending { it.documentCount }

    private fun statusBreakdown(documents: List<Document>): List<StatusCount> {
        val total = documents.size
        return documents.groupBy { it.status }
            .map { (status, docs) ->
                StatusCount(
                    status = status,
                    count = docs.size,
                    percent = percent(docs.size, total)
                )
            }
            .sortedByDescending { it.count }
    }

    private fun topOwners(documents: List<Document>): List<OwnerStat> =
        documents
            .mapNotNull { it.attributes.owner.trim().ifBlank { null } }
            .groupingBy { it }
            .eachCount()
            .map { (owner, count) -> OwnerStat(owner = owner, documentCount = count) }
            .sortedByDescending { it.documentCount }
            .take(5)

    private fun percent(part: Int, total: Int): Int =
        if (total == 0) 0 else (part * 100) / total
}
