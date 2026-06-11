package com.shelldocs.feature.assistant.presentation

import com.shelldocs.core.common.mvi.MviIntent

sealed interface AssistantIntent : MviIntent {
    data object Initialize : AssistantIntent
    data class InputChanged(val value: String) : AssistantIntent
    data object SendQuestion : AssistantIntent
    data class SelectConversation(val conversationId: String) : AssistantIntent
    data object StartNewConversation : AssistantIntent
}
