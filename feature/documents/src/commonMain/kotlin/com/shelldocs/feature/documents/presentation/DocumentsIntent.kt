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
    data object StartCreatingDocument : DocumentsIntent
    data class CreateDocument(val title: String) : DocumentsIntent
    data class NewDocumentTitleChanged(val value: String) : DocumentsIntent
    data class NewDocumentMarkdownChanged(val value: String) : DocumentsIntent
    data object CancelNewDocument : DocumentsIntent
    data object SubmitNewDocument : DocumentsIntent
    data object ToggleExplorerPanel : DocumentsIntent
    data object ToggleAttributesPanel : DocumentsIntent
    data object OpenAttributesEditor : DocumentsIntent
    data object CloseAttributesEditor : DocumentsIntent
    data class AttributesOwnerChanged(val value: String) : DocumentsIntent
    data class AttributesModuleChanged(val value: String) : DocumentsIntent
    data class AttributesTeamChanged(val value: String) : DocumentsIntent
    data class AttributesPlatformChanged(val value: String) : DocumentsIntent
    data class AttributesTagsChanged(val value: String) : DocumentsIntent
    data object SaveAttributes : DocumentsIntent
    data object DismissError : DocumentsIntent
}
