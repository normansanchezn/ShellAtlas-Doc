package com.shelldocs.feature.updates.presentation

import com.shelldocs.core.common.error.toErrorDialogState
import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.mvi.MviViewModel
import com.shelldocs.core.common.result.onFailure
import com.shelldocs.core.common.result.onSuccess
import com.shelldocs.core.domain.usecase.updates.GetPendingUpdatesUseCase
import com.shelldocs.core.domain.usecase.updates.ScanForUpdatesUseCase
import kotlinx.coroutines.withContext

class UpdatesViewModel(
    private val getPendingUpdates: GetPendingUpdatesUseCase,
    private val scanForUpdates: ScanForUpdatesUseCase,
    dispatchers: DispatcherProvider,
) : MviViewModel<UpdatesIntent, UpdatesState, UpdatesEffect>(UpdatesState(), dispatchers) {

    override suspend fun handleIntent(intent: UpdatesIntent) {
        when (intent) {
            UpdatesIntent.Initialize -> load()
            UpdatesIntent.ScanNow -> scan()
            UpdatesIntent.DismissError -> setState { copy(errorDialog = null) }
            is UpdatesIntent.ToggleRiskFilter ->
                setState { copy(riskFilter = if (riskFilter == intent.risk) null else intent.risk) }
        }
    }

    private suspend fun load() {
        setState { copy(isLoading = true, errorDialog = null) }
        withContext(dispatchers.default) {
            getPendingUpdates()
        }
            .onSuccess { updates -> setState { copy(isLoading = false, updates = updates) } }
            .onFailure { error ->
                setState { copy(isLoading = false, errorDialog = error.toErrorDialogState("load pending updates")) }
            }
    }

    private suspend fun scan() {
        setState { copy(isScanning = true, errorDialog = null) }
        withContext(dispatchers.default) {
            scanForUpdates()
        }
            .onSuccess { updates -> setState { copy(isScanning = false, updates = updates) } }
            .onFailure { error ->
                setState { copy(isScanning = false, errorDialog = error.toErrorDialogState("scan for updates")) }
            }
    }
}
