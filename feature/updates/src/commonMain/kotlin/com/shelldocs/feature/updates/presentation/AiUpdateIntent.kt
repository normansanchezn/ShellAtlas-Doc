package com.shelldocs.feature.updates.presentation

import com.shelldocs.core.common.mvi.MviIntent

sealed interface AiUpdateIntent : MviIntent {
    data class Initialize(val documentId: String) : AiUpdateIntent
    data class EditLine(val index: Int, val text: String) : AiUpdateIntent
    data class RemoveLine(val index: Int) : AiUpdateIntent
    data object RequestApply : AiUpdateIntent
    data object ConfirmApply : AiUpdateIntent
    data object CancelApply : AiUpdateIntent
    data object DismissError : AiUpdateIntent
}
