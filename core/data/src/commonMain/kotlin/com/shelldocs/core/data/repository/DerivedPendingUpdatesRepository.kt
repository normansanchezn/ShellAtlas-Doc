package com.shelldocs.core.data.repository

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.map
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.updates.PendingUpdate
import com.shelldocs.core.domain.entity.updates.RiskLevel
import com.shelldocs.core.domain.repository.DocumentRepository
import com.shelldocs.core.domain.repository.PendingUpdatesRepository
import com.shelldocs.core.domain.usecase.assistant.EvaluateDocumentHealthUseCase
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

/**
 * Computes the Documentation Health triage from real document health and the
 * risk rule engine: Critical when stale beyond a year, version-mismatched
 * against [latestApplicationVersion], or tagged with an unreflected upstream
 * change; Low otherwise. Medium only ever comes from [setManualRisk].
 *
 * Upstream signals (Azure DevOps work items, Program Board, Confluence,
 * Release Notes) aren't wired to real systems yet, so a sync job is expected
 * to tag affected documents with [UPSTREAM_SIGNAL_TAGS] in the meantime.
 */
class DerivedPendingUpdatesRepository(
    private val documentRepository: DocumentRepository,
    private val evaluateHealth: EvaluateDocumentHealthUseCase,
    private val timeProvider: TimeProvider,
    private val latestApplicationVersion: String = "9.6.0",
) : PendingUpdatesRepository {

    private val manualOverrides = mutableMapOf<String, RiskLevel>()

    override suspend fun pendingUpdates(): DomainResult<List<PendingUpdate>> =
        documentRepository.documents().map { documents ->
            documents.mapNotNull(::toPendingUpdate)
        }

    override suspend fun scanNow(): DomainResult<List<PendingUpdate>> = pendingUpdates()

    override suspend fun setManualRisk(documentId: String, risk: RiskLevel?): DomainResult<PendingUpdate> {
        if (risk == null) manualOverrides.remove(documentId) else manualOverrides[documentId] = risk
        return when (val result = documentRepository.document(documentId)) {
            is DomainResult.Failure -> result
            is DomainResult.Success -> DomainResult.success(toRow(result.value))
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun toPendingUpdate(document: Document): PendingUpdate? {
        val health = evaluateHealth(document)
        if (health.isHealthy) return null
        return toRow(document, health.score)
    }

    @OptIn(ExperimentalTime::class)
    private fun toRow(document: Document, healthScore: Int = evaluateHealth(document).score): PendingUpdate {
        val attributes = document.attributes
        val ageDays = ((timeProvider.now() - document.updatedAt) / 1.days).toInt()
        val reviewAgeDays = attributes.lastReviewedDate
            ?.let { ((timeProvider.now() - it) / 1.days).toInt() }
            ?: ageDays
        val versionMismatch = attributes.applicationVersion.isNotBlank() && attributes.applicationVersion != latestApplicationVersion
        val hasUnreflectedUpstreamChanges = UPSTREAM_SIGNAL_TAGS.any { it in attributes.tags }
        val autoRisk = RiskLevel.fromSignals(reviewAgeDays, versionMismatch, hasUnreflectedUpstreamChanges)
        val impact = 100 - healthScore
        return PendingUpdate(
            documentId = document.id,
            documentTitle = document.title,
            module = attributes.module,
            team = attributes.team,
            risk = manualOverrides[document.id] ?: autoRisk,
            ageDays = ageDays,
            impactScore = impact,
            ownerName = attributes.owner,
            ownerInitials = initials(attributes.owner),
            lastReview = attributes.lastReviewedDate ?: document.updatedAt,
            developmentArea = attributes.developmentArea,
            applicationVersion = latestApplicationVersion,
            documentVersion = attributes.applicationVersion,
            manualRiskOverride = manualOverrides[document.id],
        )
    }

    private fun initials(name: String): String =
        name.split(' ').filter { it.isNotBlank() }.take(2)
            .map { it.first().uppercaseChar() }.joinToString("")

    companion object {
        /** Marker tags a future sync job writes when an upstream system has unreflected changes. */
        val UPSTREAM_SIGNAL_TAGS = setOf(
            "ado-changes-pending",
            "program-board-changes-pending",
            "confluence-updated",
            "release-notes-pending",
        )
    }
}
