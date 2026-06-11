@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.feature.updates.presentation

import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.updates.PendingUpdate
import com.shelldocs.core.domain.entity.updates.RiskLevel
import com.shelldocs.core.domain.repository.PendingUpdatesRepository
import com.shelldocs.core.domain.usecase.updates.GetPendingUpdatesUseCase
import com.shelldocs.core.domain.usecase.updates.ScanForUpdatesUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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

private class FakePendingUpdatesRepository : PendingUpdatesRepository {
    var scans = 0
    private val data = listOf(
        update("a", RiskLevel.LOW, 31),
        update("b", RiskLevel.CRITICAL, 94),
        update("c", RiskLevel.HIGH, 65),
    )

    override suspend fun pendingUpdates() = DomainResult.success(data)
    override suspend fun scanNow(): DomainResult<List<PendingUpdate>> {
        scans++
        return DomainResult.success(data)
    }
}

class UpdatesViewModelTest {

    private val repository = FakePendingUpdatesRepository()

    private fun viewModel(scheduler: kotlinx.coroutines.test.TestCoroutineScheduler) = UpdatesViewModel(
        getPendingUpdates = GetPendingUpdatesUseCase(repository),
        scanForUpdates = ScanForUpdatesUseCase(repository),
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
}
