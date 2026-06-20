package com.shelldocs.feature.updates.presentation

import com.shelldocs.core.common.error.toErrorDialogState
import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.mvi.MviViewModel
import com.shelldocs.core.common.result.onFailure
import com.shelldocs.core.common.result.onSuccess
import com.shelldocs.core.domain.entity.document.MetadataAttribute
import com.shelldocs.core.domain.usecase.classification.AcceptMetadataSuggestionUseCase
import com.shelldocs.core.domain.usecase.classification.AssignMetadataUseCase
import com.shelldocs.core.domain.usecase.classification.GetMetadataIssuesUseCase
import com.shelldocs.core.domain.usecase.updates.GetPendingUpdatesUseCase
import com.shelldocs.core.domain.usecase.updates.ScanForUpdatesUseCase
import kotlinx.coroutines.withContext

class UpdatesViewModel(
    private val getPendingUpdates: GetPendingUpdatesUseCase,
    private val scanForUpdates: ScanForUpdatesUseCase,
    private val getMetadataIssues: GetMetadataIssuesUseCase,
    private val acceptMetadataSuggestion: AcceptMetadataSuggestionUseCase,
    private val assignMetadata: AssignMetadataUseCase,
    private val isAdmin: Boolean,
    dispatchers: DispatcherProvider,
) : MviViewModel<UpdatesIntent, UpdatesState, UpdatesEffect>(UpdatesState(isAdmin = isAdmin), dispatchers) {

    override suspend fun handleIntent(intent: UpdatesIntent) {
        when (intent) {
            UpdatesIntent.Initialize -> load()
            UpdatesIntent.ScanNow -> scan()
            UpdatesIntent.DismissError -> setState { copy(errorDialog = null) }
            is UpdatesIntent.ToggleRiskFilter ->
                setState { copy(riskFilter = if (riskFilter == intent.risk) null else intent.risk) }
            is UpdatesIntent.SelectTab -> selectTab(intent.tab)
            is UpdatesIntent.AcceptMetadataSuggestion -> accept(intent.documentId, intent.attribute)
            is UpdatesIntent.AssignMetadata -> assign(intent.documentId, intent.attribute, intent.value)
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
        loadMetadataIssues()
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
        loadMetadataIssues()
    }

    private suspend fun selectTab(tab: DocumentationHealthTab) {
        setState { copy(selectedTab = tab) }
        if (tab == DocumentationHealthTab.METADATA_ISSUES && currentState.metadataIssues.isEmpty()) {
            loadMetadataIssues()
        }
    }

    private suspend fun loadMetadataIssues() {
        setState { copy(isLoadingMetadataIssues = true) }
        withContext(dispatchers.default) {
            getMetadataIssues()
        }
            .onSuccess { issues -> setState { copy(isLoadingMetadataIssues = false, metadataIssues = issues) } }
            .onFailure { error ->
                setState { copy(isLoadingMetadataIssues = false, errorDialog = error.toErrorDialogState("load metadata issues")) }
            }
    }

    private suspend fun accept(documentId: String, attribute: MetadataAttribute) {
        withContext(dispatchers.default) {
            acceptMetadataSuggestion(documentId, attribute)
        }
            .onSuccess {
                sendEffect(UpdatesEffect.MetadataUpdated(documentId))
                loadMetadataIssues()
            }
            .onFailure { error ->
                setState { copy(errorDialog = error.toErrorDialogState("accept metadata suggestion")) }
            }
    }

    private suspend fun assign(documentId: String, attribute: MetadataAttribute, value: String) {
        withContext(dispatchers.default) {
            assignMetadata(documentId, attribute, value)
        }
            .onSuccess {
                sendEffect(UpdatesEffect.MetadataUpdated(documentId))
                loadMetadataIssues()
            }
            .onFailure { error ->
                setState { copy(errorDialog = error.toErrorDialogState("assign metadata")) }
            }
    }
}
