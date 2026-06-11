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
 * Computes the maintenance triage from real document health: impact is the
 * inverse of the health score, risk is bucketed from impact.
 */
class DerivedPendingUpdatesRepository(
    private val documentRepository: DocumentRepository,
    private val evaluateHealth: EvaluateDocumentHealthUseCase,
    private val timeProvider: TimeProvider,
) : PendingUpdatesRepository {

    override suspend fun pendingUpdates(): DomainResult<List<PendingUpdate>> =
        documentRepository.documents().map { documents ->
            documents.mapNotNull(::toPendingUpdate)
        }

    override suspend fun scanNow(): DomainResult<List<PendingUpdate>> = pendingUpdates()

    @OptIn(ExperimentalTime::class)
    private fun toPendingUpdate(document: Document): PendingUpdate? {
        val health = evaluateHealth(document)
        if (health.isHealthy) return null
        val impact = 100 - health.score
        return PendingUpdate(
            documentId = document.id,
            documentTitle = document.title,
            module = document.attributes.module,
            team = document.attributes.team,
            risk = RiskLevel.fromImpactScore(impact),
            ageDays = ((timeProvider.now() - document.updatedAt) / 1.days).toInt(),
            impactScore = impact,
            ownerName = document.attributes.owner,
            ownerInitials = initials(document.attributes.owner),
            lastReview = document.updatedAt,
        )
    }

    private fun initials(name: String): String =
        name.split(' ').filter { it.isNotBlank() }.take(2)
            .map { it.first().uppercaseChar() }.joinToString("")
}
