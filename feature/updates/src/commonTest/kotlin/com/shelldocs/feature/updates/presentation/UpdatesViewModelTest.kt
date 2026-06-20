@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.feature.updates.presentation

import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.DocumentClassificationResult
import com.shelldocs.core.domain.entity.document.MetadataAttribute
import com.shelldocs.core.domain.entity.updates.PendingUpdate
import com.shelldocs.core.domain.entity.updates.RiskLevel
import com.shelldocs.core.domain.repository.DocumentClassificationRepository
import com.shelldocs.core.domain.repository.PendingUpdatesRepository
import com.shelldocs.core.domain.usecase.classification.AcceptMetadataSuggestionUseCase
import com.shelldocs.core.domain.usecase.classification.AssignMetadataUseCase
import com.shelldocs.core.domain.usecase.classification.GetMetadataIssuesUseCase
import com.shelldocs.core.domain.usecase.updates.GetPendingUpdatesUseCase
import com.shelldocs.core.domain.usecase.updates.ScanForUpdatesUseCase
import com.shelldocs.core.domain.usecase.updates.SetManualRiskLevelUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.*

private class SingleDispatcher(dispatcher: CoroutineDispatcher) : DispatcherProvider {
    override val main = dispatcher
    override val default = dispatcher
    override val io = dispatcher
}

private fun update(id: String, risk: RiskLevel, impact: Int) = PendingUpdate(
    documentId = id,
    documentTitle = "Doc $id",
    module = "Module",
    team = "Team",
    risk = risk,
    ageDays = 100,
    impactScore = impact,
    ownerName = "Elena Vargas",
    ownerInitials = "EV",
    lastReview = Instant.parse("2026-01-12T00:00:00Z"),
)

private class FakePendingUpdatesRepository(var failScan: Boolean = false) : PendingUpdatesRepository {
    var scans = 0
    private val data = listOf(
        update("a", RiskLevel.LOW, 31),
        update("b", RiskLevel.CRITICAL, 94),
        update("c", RiskLevel.HIGH, 65),
    )

    override suspend fun pendingUpdates() = DomainResult.success(data)
    override suspend fun scanNow(): DomainResult<List<PendingUpdate>> {
        if (failScan) return DomainResult.failure(AppError.Network("Scanner offline"))
        scans++
        return DomainResult.success(data)
    }

    override suspend fun setManualRisk(documentId: String, risk: RiskLevel?): DomainResult<PendingUpdate> =
        DomainResult.failure(AppError.NotFound("Not used in these tests"))
}

private class FakeDocumentClassificationRepository : DocumentClassificationRepository {
    override suspend fun classify(documentId: String): DomainResult<DocumentClassificationResult> =
        DomainResult.failure(AppError.NotFound("Not used in these tests"))

    override suspend fun metadataIssues(): DomainResult<List<DocumentClassificationResult>> =
        DomainResult.success(emptyList())

    override suspend fun acceptSuggestion(documentId: String, attribute: MetadataAttribute): DomainResult<Document> =
        DomainResult.failure(AppError.NotFound("Not used in these tests"))

    override suspend fun assignMetadata(documentId: String, attribute: MetadataAttribute, value: String): DomainResult<Document> =
        DomainResult.failure(AppError.NotFound("Not used in these tests"))
}

class UpdatesViewModelTest {

    private val repository = FakePendingUpdatesRepository(failScan = false)
    private val classificationRepository = FakeDocumentClassificationRepository()

    private fun viewModel(scheduler: kotlinx.coroutines.test.TestCoroutineScheduler) = UpdatesViewModel(
        getPendingUpdates = GetPendingUpdatesUseCase(repository),
        scanForUpdates = ScanForUpdatesUseCase(repository),
        getMetadataIssues = GetMetadataIssuesUseCase(classificationRepository),
        acceptMetadataSuggestion = AcceptMetadataSuggestionUseCase(classificationRepository),
        assignMetadata = AssignMetadataUseCase(classificationRepository),
        setManualRiskLevel = SetManualRiskLevelUseCase(repository),
        currentUserRole = UserRole.VIEWER,
        visibleDevelopmentArea = null,
        canUpdateDocuments = false,
        dispatchers = SingleDispatcher(StandardTestDispatcher(scheduler)),
    )

    @Test
    fun initializeSortsByRiskThenImpact() = runTest {
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(UpdatesIntent.Initialize)
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("b", "c", "a"), viewModel.currentState.updates.map { it.documentId })
        assertEquals(1, viewModel.currentState.countsByRisk[RiskLevel.CRITICAL])
        viewModel.clear()
    }

    @Test
    fun riskFilterTogglesOnAndOff() = runTest {
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(UpdatesIntent.Initialize)
        viewModel.onIntent(UpdatesIntent.ToggleRiskFilter(RiskLevel.CRITICAL))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("b"), viewModel.currentState.filteredUpdates.map { it.documentId })

        viewModel.onIntent(UpdatesIntent.ToggleRiskFilter(RiskLevel.CRITICAL))
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.currentState.riskFilter)
        assertEquals(3, viewModel.currentState.filteredUpdates.size)
        viewModel.clear()
    }

    @Test
    fun scanNowHitsTheRepository() = runTest {
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(UpdatesIntent.ScanNow)
        testScheduler.advanceUntilIdle()

        assertEquals(1, repository.scans)
        viewModel.clear()
    }

    @Test
    fun scanSetsLoadingFlagThenClears() = runTest {
        val viewModel = viewModel(testScheduler)

        viewModel.onIntent(UpdatesIntent.ScanNow)
        testScheduler.runCurrent()

        assertTrue(viewModel.currentState.isScanning)

        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.currentState.isScanning)
        viewModel.clear()
    }

    @Test
    fun scanFailureShowsErrorDialog() = runTest {
        repository.failScan = true
        val viewModel = viewModel(testScheduler)

        viewModel.onIntent(UpdatesIntent.ScanNow)
        testScheduler.advanceUntilIdle()

        assertNotNull(viewModel.currentState.errorDialog)
        assertFalse(viewModel.currentState.isScanning)
        viewModel.clear()
    }

    @Test
    fun dismissErrorClearsDialog() = runTest {
        repository.failScan = true
        val viewModel = viewModel(testScheduler)

        viewModel.onIntent(UpdatesIntent.ScanNow)
        testScheduler.advanceUntilIdle()

        assertNotNull(viewModel.currentState.errorDialog)

        viewModel.onIntent(UpdatesIntent.DismissError)
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.currentState.errorDialog)
        viewModel.clear()
    }
}
