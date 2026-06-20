package com.shelldocs.feature.updates.presentation

import com.shelldocs.core.common.error.ErrorDialogState
import com.shelldocs.core.common.mvi.MviState
import com.shelldocs.core.domain.entity.document.SuggestionLine

/** Snapshot of the AI Suggested Update split-screen editor. */
data class AiUpdateState(
    val isLoading: Boolean = true,
    val isApplying: Boolean = false,
    val documentId: String = "",
    val documentTitle: String = "",
    val ownerName: String = "",
    val currentMarkdown: String = "",
    val suggestedLines: List<SuggestionLine> = emptyList(),
    val showConfirmDialog: Boolean = false,
    val errorDialog: ErrorDialogState? = null,
    val isAdmin: Boolean = false,
) : MviState {

    val editedMarkdown: String get() = suggestedLines.joinToString("\n") { it.text }

    /** Apply Update is enabled only while the AI editor still has content. */
    val canApply: Boolean get() = editedMarkdown.isNotBlank() && !isApplying
}
