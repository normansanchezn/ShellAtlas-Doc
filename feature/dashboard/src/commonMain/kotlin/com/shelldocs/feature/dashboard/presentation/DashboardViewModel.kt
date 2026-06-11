package com.shelldocs.feature.dashboard.presentation

import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.mvi.MviViewModel
import com.shelldocs.core.common.result.onFailure
import com.shelldocs.core.common.result.onSuccess
import com.shelldocs.core.domain.usecase.dashboard.GetDashboardMetricsUseCase

class DashboardViewModel(
    private val getDashboardMetrics: GetDashboardMetricsUseCase,
    dispatchers: DispatcherProvider,
) : MviViewModel<DashboardIntent, DashboardState, DashboardEffect>(DashboardState(), dispatchers) {

    override suspend fun handleIntent(intent: DashboardIntent) {
        when (intent) {
            DashboardIntent.Initialize, DashboardIntent.Refresh -> load()
        }
    }

    private suspend fun load() {
        setState { copy(isLoading = true, errorMessage = null) }
        getDashboardMetrics()
            .onSuccess { metrics -> setState { copy(isLoading = false, metrics = metrics) } }
            .onFailure { error -> setState { copy(isLoading = false, errorMessage = error.message) } }
    }
}
