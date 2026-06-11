package com.shelldocs.feature.dashboard.presentation

import com.shelldocs.core.common.error.toErrorDialogState
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
            DashboardIntent.DismissError -> setState { copy(errorDialog = null) }
        }
    }

    private suspend fun load() {
        setState { copy(isLoading = true, errorDialog = null) }
        getDashboardMetrics()
            .onSuccess { metrics -> setState { copy(isLoading = false, metrics = metrics) } }
            .onFailure { error ->
                setState { copy(isLoading = false, errorDialog = error.toErrorDialogState("load the dashboard")) }
            }
    }
}
