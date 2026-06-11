package com.shelldocs.feature.sources.presentation

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
        }
    }

    private suspend fun load() {
        setState { copy(isLoading = true, errorMessage = null) }
        val sources = getSources().getOrDefault(emptyList())
        val log = getSyncLog().getOrDefault(emptyList())
        setState { copy(isLoading = false, sources = sources, syncLog = log) }
    }

    private suspend fun sync(sourceId: String) {
        setState { copy(syncingSourceIds = syncingSourceIds + sourceId, errorMessage = null) }
        syncSource(roleProvider(), sourceId)
            .onSuccess { synced ->
                refreshSource(synced.id)
                sendEffect(SourcesEffect.ShowNotice("${synced.kind.displayName} synced"))
            }
            .onFailure { error -> setState { copy(errorMessage = error.message) } }
        setState { copy(syncingSourceIds = syncingSourceIds - sourceId) }
    }

    private suspend fun reconnect(sourceId: String) {
        sourcesRepository.reconnect(sourceId)
            .onSuccess { reconnected ->
                refreshSource(reconnected.id)
                sendEffect(SourcesEffect.ShowNotice("${reconnected.kind.displayName} reconnected"))
            }
            .onFailure { error -> setState { copy(errorMessage = error.message) } }
    }

    private suspend fun refreshSource(sourceId: String) {
        val sources = getSources().getOrDefault(currentState.sources)
        val log = getSyncLog().getOrDefault(currentState.syncLog)
        setState { copy(sources = sources, syncLog = log) }
    }
}
