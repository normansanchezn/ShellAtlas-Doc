package com.shelldocs.feature.sources.presentation

import com.shelldocs.core.common.mvi.MviIntent

sealed interface SourcesIntent : MviIntent {
    data object Initialize : SourcesIntent
    data object Refresh : SourcesIntent
    data object ContactSupport : SourcesIntent
    data object DismissError : SourcesIntent
}
