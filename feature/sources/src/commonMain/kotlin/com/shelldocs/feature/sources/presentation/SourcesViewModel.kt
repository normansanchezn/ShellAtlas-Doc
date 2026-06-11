package com.shelldocs.feature.sources.presentation

import com.shelldocs.core.common.error.toErrorDialogState
import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.mvi.MviViewModel
import com.shelldocs.core.common.result.getOrDefault
import com.shelldocs.core.common.result.onFailure
import com.shelldocs.core.common.result.onSuccess
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.repository.SourcesRepository
import com.shelldocs.core.domain.usecase.source.GetSourcesUseCase
import com.shelldocs.core.domain.usecase.source.GetSyncLogUseCase
import com.shelldocs.core.domain.usecase.source.SyncSourceUseCase

class SourcesViewModel(
    private val getSources: GetSourcesUseCase,
    private val getSyncLog: GetSyncLogUseCase,
    private val syncSource: SyncSourceUseCase,
    private val sourcesRepository: SourcesRepository,
    private val roleProvider: () -> UserRole,
    dispatchers: DispatcherProvider,
) : MviViewModel<SourcesIntent, SourcesState, SourcesEffect>(SourcesState(), dispatchers) {

    override suspend fun handleIntent(intent: SourcesIntent) {
        when (intent) {
            SourcesIntent.Initialize -> load()
            is SourcesIntent.Sync -> sync(intent.sourceId)
            is SourcesIntent.Reconnect -> reconnect(intent.sourceId)
            SourcesIntent.DismissError -> setState { copy(errorDialog = null) }
        }
    }

    private suspend fun load() {
        setState { copy(isLoading = true, errorDialog = null) }
        val sourcesResult = getSources()
        val logResult = getSyncLog()
        setState {
            copy(
                isLoading = false,
                sources = sourcesResult.getOrDefault(emptyList()),
                syncLog = logResult.getOrDefault(emptyList()),
                errorDialog = when {
                    sourcesResult is com.shelldocs.core.common.result.DomainResult.Failure ->
                        sourcesResult.error.toErrorDialogState("load the sources")
                    logResult is com.shelldocs.core.common.result.DomainResult.Failure ->
                        logResult.error.toErrorDialogState("load the sync activity")
                    else -> null
                },
            )
        }
    }

    private suspend fun sync(sourceId: String) {
        val sourceName = currentState.sources.firstOrNull { it.id == sourceId }?.kind?.displayName ?: "source"
        setState {
            copy(
                syncingSourceIds = syncingSourceIds + sourceId,
                loadingMessage = "Syncing $sourceName...",
                errorDialog = null,
            )
        }
        syncSource(roleProvider(), sourceId)
            .onSuccess { synced ->
                refreshSource(synced.id)
                sendEffect(SourcesEffect.ShowNotice("${synced.kind.displayName} synced"))
            }
            .onFailure { error ->
                setState {
                    copy(
                        errorDialog = error.toErrorDialogState("sync $sourceName"),
                    )
                }
            }
        setState { copy(syncingSourceIds = syncingSourceIds - sourceId, loadingMessage = null) }
    }

    private suspend fun reconnect(sourceId: String) {
        val sourceName = currentState.sources.firstOrNull { it.id == sourceId }?.kind?.displayName ?: "integration"
        setState { copy(loadingMessage = "Reconnecting $sourceName...", errorDialog = null) }
        sourcesRepository.reconnect(sourceId)
            .onSuccess { reconnected ->
                refreshSource(reconnected.id)
                sendEffect(SourcesEffect.ShowNotice("${reconnected.kind.displayName} reconnected"))
            }
            .onFailure { error ->
                setState {
                    copy(
                        errorDialog = error.toErrorDialogState("reconnect $sourceName"),
                    )
                }
            }
        setState { copy(loadingMessage = null) }
    }

    private suspend fun refreshSource(sourceId: String) {
        val sources = getSources().getOrDefault(currentState.sources)
        val log = getSyncLog().getOrDefault(currentState.syncLog)
        setState { copy(sources = sources, syncLog = log) }
    }
}
