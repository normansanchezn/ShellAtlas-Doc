package com.shelldocs.feature.sources.presentation

import com.shelldocs.core.common.mvi.MviEffect

sealed interface SourcesEffect : MviEffect {
    data class ShowNotice(val message: String) : SourcesEffect
}
