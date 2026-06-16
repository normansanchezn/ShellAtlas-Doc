package com.shelldocs.feature.dashboard.presentation

import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.dashboard.DashboardMetrics
import com.shelldocs.core.domain.entity.dashboard.StatusBreakdown
import com.shelldocs.core.domain.repository.DashboardRepository
import com.shelldocs.core.domain.usecase.dashboard.GetDashboardMetricsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private class SingleDispatcher(dispatcher: CoroutineDispatcher) : DispatcherProvider {
    override val main = dispatcher
    override val default = dispatcher
    override val io = dispatcher
}

private fun metrics(total: Int) = DashboardMetrics(
    totalDocuments = total,
    totalDocumentsDelta = 8,
    outdatedDocuments = 18,
    outdatedDocumentsDelta = 3,
    coverageScorePercent = 68,
    aiQueriesThisWeek = 371,
    aiQueriesDeltaPercent = 24,
    knowledgeHealthScore = 74,
    docsReviewedPercent = 89,
    sourcesSynced = 3,
    sourcesTotal = 3,
    aiAccuracyPercent = 94,
    staleRatePercent = 12,
    moduleCoverage = emptyList(),
    statusBreakdown = StatusBreakdown(68, 18, 9, 5),
    usage = emptyList(),
    recentActivity = emptyList(),
    attentionItems = emptyList(),
)

private class FakeDashboardRepository : DashboardRepository {
    var nextResult: DomainResult<DashboardMetrics> = DomainResult.success(metrics(147))
    override suspend fun metrics(): DomainResult<DashboardMetrics> = nextResult
}

class DashboardViewModelTest {

    private val repository = FakeDashboardRepository()

    private fun viewModel(scheduler: kotlinx.coroutines.test.TestCoroutineScheduler) = DashboardViewModel(
        getDashboardMetrics = GetDashboardMetricsUseCase(repository),
        dispatchers = SingleDispatcher(StandardTestDispatcher(scheduler)),
    )

    @Test
    fun initializeLoadsMetrics() = runTest {
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(DashboardIntent.Initialize)
        testScheduler.advanceUntilIdle()

        assertEquals(147, viewModel.currentState.metrics?.totalDocuments)
        assertFalse(viewModel.currentState.isLoading)
        assertNull(viewModel.currentState.errorDialog)
        viewModel.clear()
    }

    @Test
    fun refreshReplacesMetrics() = runTest {
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(DashboardIntent.Initialize)
        testScheduler.advanceUntilIdle()

        repository.nextResult = DomainResult.success(metrics(150))
        viewModel.onIntent(DashboardIntent.Refresh)
        testScheduler.advanceUntilIdle()

        assertEquals(150, viewModel.currentState.metrics?.totalDocuments)
        viewModel.clear()
    }

    @Test
    fun failureExposesErrorMessage() = runTest {
        repository.nextResult = DomainResult.failure(AppError.Network("offline"))
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(DashboardIntent.Initialize)
        testScheduler.advanceUntilIdle()

        assertNotNull(viewModel.currentState.errorDialog)
        viewModel.clear()
    }

    @Test
    fun dismissErrorClearsDialog() = runTest {
        repository.nextResult = DomainResult.failure(AppError.Network("offline"))
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(DashboardIntent.Initialize)
        testScheduler.advanceUntilIdle()

        assertNotNull(viewModel.currentState.errorDialog)

        viewModel.onIntent(DashboardIntent.DismissError)
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.currentState.errorDialog)
        viewModel.clear()
    }

    @Test
    fun refreshKeepsPreviousMetricsUntilComplete() = runTest {
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(DashboardIntent.Initialize)
        testScheduler.advanceUntilIdle()

        assertEquals(147, viewModel.currentState.metrics?.totalDocuments)

        repository.nextResult = DomainResult.success(metrics(200))
        viewModel.onIntent(DashboardIntent.Refresh)
        testScheduler.runCurrent()

        assertEquals(147, viewModel.currentState.metrics?.totalDocuments)
        assertTrue(viewModel.currentState.isLoading)

        testScheduler.advanceUntilIdle()

        assertEquals(200, viewModel.currentState.metrics?.totalDocuments)
        assertFalse(viewModel.currentState.isLoading)
        viewModel.clear()
    }
}
