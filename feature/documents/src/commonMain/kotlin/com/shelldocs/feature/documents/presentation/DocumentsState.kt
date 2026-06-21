package com.shelldocs.feature.documents.presentation

import com.shelldocs.core.common.error.ErrorDialogState
import com.shelldocs.core.common.mvi.MviState
import com.shelldocs.core.domain.entity.auth.Permission
import com.shelldocs.core.domain.entity.auth.RolePermissions
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.entity.document.Area
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.DocumentNode
import com.shelldocs.core.domain.entity.document.DocumentVersion

/** Editable draft of a document's attributes, shown in the attributes dialog. */
data class AttributesDraft(
    val owner: String = "",
    val module: String = "",
    val team: String = "",
    val platform: String = "",
    val tagsText: String = "",
    val area: Area? = null,
    val applicationVersion: String = "",
) {
    /** Area, Platform and Version drive the Confluence folder hierarchy — without them documents land under "Unsorted". */
    val isComplete: Boolean = area != null && platform.isNotBlank() && applicationVersion.isNotBlank()
}

enum class DocumentsEditorStep {
    Edit,
    Preview,
}

enum class AttributesEditorTarget {
    ExistingDocument,
    NewDocument,
}

/** Snapshot of the three-pane Documents screen. */
data class DocumentsState(
    val isLoading: Boolean = false,
    val loadingMessage: String? = null,
    val role: UserRole = UserRole.VIEWER,
    val tree: DocumentNode? = null,
    val documents: List<Document> = emptyList(),
    val filterQuery: String = "",
    val expandedFolders: Set<String> = emptySet(),
    val selectedDocument: Document? = null,
    val isCreatingDocument: Boolean = false,
    val newDocumentTitle: String = "",
    val newDocumentMarkdown: String = "",
    val newDocumentStep: DocumentsEditorStep = DocumentsEditorStep.Edit,
    val isEditing: Boolean = false,
    val editorMarkdown: String = "",
    val editorStep: DocumentsEditorStep = DocumentsEditorStep.Edit,
    val draftMessage: String? = null,
    val versions: List<DocumentVersion> = emptyList(),
    val isHistoryVisible: Boolean = false,
    val errorDialog: ErrorDialogState? = null,
    val isExplorerExpanded: Boolean = true,
    val isAttributesExpanded: Boolean = true,
    val isAttributesDialogOpen: Boolean = false,
    val shouldShowPreviewAfterAttributes: Boolean = false,
    val attributesEditorTarget: AttributesEditorTarget = AttributesEditorTarget.ExistingDocument,
    val attributesDraft: AttributesDraft = AttributesDraft(),
    val bookmarkedDocumentIds: Set<String> = emptySet(),
    val pendingDeleteDocumentId: String? = null,
    val pendingDeleteDocumentTitle: String = "",
    val isDeletingDocument: Boolean = false,
) : MviState {

    val filteredDocuments: List<Document> =
        if (filterQuery.isBlank()) {
            documents
        } else {
            documents.filter { document ->
                document.title.contains(filterQuery, ignoreCase = true) ||
                    document.summary.contains(filterQuery, ignoreCase = true)
            }
        }

    /** [tree] pruned to documents matching [filterQuery]; empty folders are dropped. */
    val filteredTree: DocumentNode? =
        if (filterQuery.isBlank()) {
            tree
        } else {
            tree?.pruneToDocuments(filteredDocuments.map { it.id }.toSet())
        }

    val canEdit: Boolean = RolePermissions.isGranted(role, Permission.EDIT_DOCUMENTS)

    val canPublish: Boolean = RolePermissions.isGranted(role, Permission.PUBLISH_DOCUMENTS)

    val canDelete: Boolean = RolePermissions.isGranted(role, Permission.DELETE_DOCUMENTS)

    val isBusy: Boolean = isLoading || loadingMessage != null
}
