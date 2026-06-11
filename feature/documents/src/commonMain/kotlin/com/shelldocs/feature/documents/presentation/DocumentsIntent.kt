package com.shelldocs.feature.documents.presentation

import com.shelldocs.core.common.mvi.MviIntent

sealed interface DocumentsIntent : MviIntent {
    data object Initialize : DocumentsIntent
    data class FilterChanged(val query: String) : DocumentsIntent
    data class ToggleFolder(val folderId: String) : DocumentsIntent
    data class SelectDocument(val documentId: String) : DocumentsIntent
    data object StartEditing : DocumentsIntent
    data class EditorChanged(val markdown: String) : DocumentsIntent
    data object SaveDraft : DocumentsIntent
    data class Publish(val changeSummary: String) : DocumentsIntent
    data object CancelEditing : DocumentsIntent
    data object ShowHistory : DocumentsIntent
    data object HideHistory : DocumentsIntent
    data class RestoreVersion(val versionId: String) : DocumentsIntent
    data class CreateDocument(val title: String) : DocumentsIntent
}
