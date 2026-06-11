package com.shelldocs.feature.assistant.presentation

import com.shelldocs.core.common.mvi.MviEffect

sealed interface AssistantEffect : MviEffect {
    data object ScrollToLatestMessage : AssistantEffect
}
