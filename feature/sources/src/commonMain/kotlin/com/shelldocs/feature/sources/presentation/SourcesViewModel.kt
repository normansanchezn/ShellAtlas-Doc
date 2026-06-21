package com.shelldocs.feature.sources.presentation

import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.error.toErrorDialogState
import com.shelldocs.core.common.mvi.MviViewModel
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.getOrDefault
import com.shelldocs.core.domain.repository.ConnectionsRepository
import kotlinx.coroutines.withContext

class SourcesViewModel(
    private val connectionsRepository: ConnectionsRepository,
    dispatchers: DispatcherProvider,
) : MviViewModel<SourcesIntent, SourcesState, SourcesEffect>(SourcesState(), dispatchers) {

    override suspend fun handleIntent(intent: SourcesIntent) {
        when (intent) {
            SourcesIntent.Initialize -> load(showSpinner = true)
            SourcesIntent.Refresh -> load(showSpinner = false)
            SourcesIntent.ContactSupport -> Unit // no-op for now
            SourcesIntent.DismissError -> setState { copy(errorDialog = null) }
        }
    }

    private suspend fun load(showSpinner: Boolean) {
        setState {
            copy(
                isLoading = showSpinner,
                loadingMessage = if (showSpinner) null else "Refreshing...",
                errorDialog = null
            )
        }
        val result = withContext(dispatchers.io) { connectionsRepository.statuses() }
        setState {
            copy(
                isLoading = false,
                loadingMessage = null,
                connections = result.getOrDefault(connections),
                errorDialog = (result as? DomainResult.Failure)?.error?.toErrorDialogState("load connection status"),
            )
        }
    }
}
