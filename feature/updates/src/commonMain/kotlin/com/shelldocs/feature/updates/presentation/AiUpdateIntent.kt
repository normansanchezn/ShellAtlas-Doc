package com.shelldocs.feature.updates.presentation

import com.shelldocs.core.common.mvi.MviIntent
import com.shelldocs.core.domain.entity.document.DocumentAttributes

sealed interface AiUpdateIntent : MviIntent {
    data class Initialize(val documentId: String) : AiUpdateIntent
    data class EditMarkdown(val markdown: String) : AiUpdateIntent
    data object SaveChanges : AiUpdateIntent
    data class ConfirmMetadata(val attributes: DocumentAttributes) : AiUpdateIntent
    data object CancelMetadata : AiUpdateIntent
    data object ConfirmApply : AiUpdateIntent
    data object CancelApply : AiUpdateIntent
    data object DismissError : AiUpdateIntent
}
