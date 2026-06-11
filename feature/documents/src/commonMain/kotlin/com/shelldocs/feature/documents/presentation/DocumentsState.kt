package com.shelldocs.feature.documents.presentation

import com.shelldocs.core.common.mvi.MviState
import com.shelldocs.core.domain.entity.auth.Permission
import com.shelldocs.core.domain.entity.auth.RolePermissions
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.DocumentNode
import com.shelldocs.core.domain.entity.document.DocumentVersion

/** Snapshot of the three-pane Documents screen. */
data class DocumentsState(
    val isLoading: Boolean = false,
    val role: UserRole = UserRole.VIEWER,
    val tree: DocumentNode? = null,
    val documents: List<Document> = emptyList(),
    val filterQuery: String = "",
    val expandedFolders: Set<String> = emptySet(),
    val selectedDocument: Document? = null,
    val isEditing: Boolean = false,
    val editorMarkdown: String = "",
    val draftMessage: String? = null,
    val versions: List<DocumentVersion> = emptyList(),
    val isHistoryVisible: Boolean = false,
    val errorMessage: String? = null,
    val isExplorerExpanded: Boolean = true,
    val isAttributesExpanded: Boolean = true,
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

    val canEdit: Boolean = RolePermissions.isGranted(role, Permission.EDIT_DOCUMENTS)

    val canPublish: Boolean = RolePermissions.isGranted(role, Permission.PUBLISH_DOCUMENTS)
}
