package com.shelldocs.feature.settings.presentation

import com.shelldocs.core.common.mvi.MviEffect

sealed interface SettingsEffect : MviEffect {
    data object SignedOut : SettingsEffect
}
