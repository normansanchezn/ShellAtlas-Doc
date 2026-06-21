@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.core.domain.usecase.updates

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.updates.PendingUpdate
import com.shelldocs.core.domain.entity.updates.RiskLevel
import com.shelldocs.core.domain.repository.PendingUpdatesRepository
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeHealthyDocumentsRepository(
    private val rows: List<PendingUpdate>,
) : PendingUpdatesRepository {
    override suspend fun pendingUpdates(): DomainResult<List<PendingUpdate>> = DomainResult.success(emptyList())
    override suspend fun healthyDocuments(): DomainResult<List<PendingUpdate>> = DomainResult.success(rows)
    override suspend fun scanNow(): DomainResult<List<PendingUpdate>> = DomainResult.success(emptyList())
    override suspend fun setManualRisk(documentId: String, risk: RiskLevel?): DomainResult<PendingUpdate> =
        DomainResult.success(rows.first())
}

class GetHealthyDocumentsUseCaseTest {

    @Test
    fun removesDuplicateDocumentsBeforeSorting() = kotlinx.coroutines.test.runTest {
        val rows = listOf(
            healthyRow("b", "Beta"),
            healthyRow("a", "Alpha"),
            healthyRow("a", "Alpha Duplicate"),
        )
        val useCase = GetHealthyDocumentsUseCase(FakeHealthyDocumentsRepository(rows))

        val result = useCase()
        val documents = (result as DomainResult.Success).value

        assertEquals(listOf("a", "b"), documents.map { it.documentId })
    }
}

private fun healthyRow(id: String, title: String) = PendingUpdate(
    documentId = id,
    documentTitle = title,
    module = "Docs",
    team = "Engineering",
    risk = RiskLevel.LOW,
    ageDays = 4,
    impactScore = 8,
    ownerName = "Norman Sanchez",
    ownerInitials = "NS",
    lastReview = Instant.parse("2026-06-20T00:00:00Z"),
)
