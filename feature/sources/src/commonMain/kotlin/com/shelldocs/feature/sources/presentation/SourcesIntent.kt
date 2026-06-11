package com.shelldocs.feature.sources.presentation

import com.shelldocs.core.common.mvi.MviIntent

sealed interface SourcesIntent : MviIntent {
    data object Initialize : SourcesIntent
    data class Sync(val sourceId: String) : SourcesIntent
    data class Reconnect(val sourceId: String) : SourcesIntent
}
