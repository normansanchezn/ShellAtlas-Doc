package com.shelldocs.feature.updates.presentation

import com.shelldocs.core.common.mvi.MviEffect

sealed interface AiUpdateEffect : MviEffect {
    data object UpdateApplied : AiUpdateEffect
}
