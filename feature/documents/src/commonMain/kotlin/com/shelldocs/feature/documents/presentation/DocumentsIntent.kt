package com.shelldocs.feature.documents.presentation

import com.shelldocs.core.common.mvi.MviIntent
import com.shelldocs.core.domain.entity.document.Area

sealed interface DocumentsIntent : MviIntent {
    data object Initialize : DocumentsIntent
    data class FilterChanged(val query: String) : DocumentsIntent
    data class ToggleFolder(val folderId: String) : DocumentsIntent
    data class SelectDocument(val documentId: String) : DocumentsIntent
    data object StartEditing : DocumentsIntent
    data class EditorChanged(val markdown: String) : DocumentsIntent
    data object ContinueToPreview : DocumentsIntent
    data object BackToEditor : DocumentsIntent
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
    data object ContinueNewDocumentToPreview : DocumentsIntent
    data object BackToNewDocumentEditor : DocumentsIntent
    data object CancelNewDocument : DocumentsIntent
    data object SubmitNewDocument : DocumentsIntent
    data object OpenNewDocumentAttributesEditor : DocumentsIntent
    data object ToggleExplorerPanel : DocumentsIntent
    data object ToggleAttributesPanel : DocumentsIntent
    data object OpenAttributesEditor : DocumentsIntent
    data object CloseAttributesEditor : DocumentsIntent
    data class AttributesOwnerChanged(val value: String) : DocumentsIntent
    data class AttributesModuleChanged(val value: String) : DocumentsIntent
    data class AttributesTeamChanged(val value: String) : DocumentsIntent
    data class AttributesPlatformChanged(val value: String) : DocumentsIntent
    data class AttributesTagsChanged(val value: String) : DocumentsIntent
    data class AttributesAreaChanged(val value: Area?) : DocumentsIntent
    data class AttributesVersionChanged(val value: String) : DocumentsIntent
    data object SaveAttributes : DocumentsIntent
    data object DismissError : DocumentsIntent
    data class ToggleBookmark(val documentId: String) : DocumentsIntent
    data object ExportPdf : DocumentsIntent
    data class BreadcrumbNavigate(val folderId: String?) : DocumentsIntent
    data class RequestDeleteDocument(val documentId: String, val documentTitle: String) : DocumentsIntent
    data object ConfirmDeleteDocument : DocumentsIntent
    data object CancelDeleteDocument : DocumentsIntent
}
