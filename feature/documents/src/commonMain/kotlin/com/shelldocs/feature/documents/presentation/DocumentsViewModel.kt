package com.shelldocs.feature.documents.presentation

import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.mvi.MviViewModel
import com.shelldocs.core.common.result.getOrDefault
import com.shelldocs.core.common.result.onFailure
import com.shelldocs.core.common.result.onSuccess
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.entity.document.DocumentNode
import com.shelldocs.core.domain.usecase.document.CreateDocumentUseCase
import com.shelldocs.core.domain.usecase.document.GetDocumentTreeUseCase
import com.shelldocs.core.domain.usecase.document.GetDocumentVersionsUseCase
import com.shelldocs.core.domain.usecase.document.GetDocumentsUseCase
import com.shelldocs.core.domain.usecase.document.PublishDocumentUseCase
import com.shelldocs.core.domain.usecase.document.RestoreDocumentVersionUseCase
import com.shelldocs.core.domain.usecase.document.SaveDraftUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DocumentsViewModel(
    private val getDocuments: GetDocumentsUseCase,
    private val getDocumentTree: GetDocumentTreeUseCase,
    private val saveDraft: SaveDraftUseCase,
    private val publishDocument: PublishDocumentUseCase,
    private val getVersions: GetDocumentVersionsUseCase,
    private val restoreVersion: RestoreDocumentVersionUseCase,
    private val createDocument: CreateDocumentUseCase,
    private val roleProvider: () -> UserRole,
    dispatchers: DispatcherProvider,
    openDocumentRequests: StateFlow<String?> = MutableStateFlow(null),
    private val consumeOpenDocumentRequest: () -> Unit = {},
) : MviViewModel<DocumentsIntent, DocumentsState, DocumentsEffect>(DocumentsState(), dispatchers) {

    init {
        scope.launch {
            openDocumentRequests.collect { documentId ->
                if (documentId != null) {
                    if (currentState.documents.isEmpty()) initialize()
                    select(documentId)
                    consumeOpenDocumentRequest()
                }
            }
        }
    }

    override suspend fun handleIntent(intent: DocumentsIntent) {
        when (intent) {
            DocumentsIntent.Initialize -> initialize()
            is DocumentsIntent.FilterChanged -> setState { copy(filterQuery = intent.query) }
            is DocumentsIntent.ToggleFolder -> toggleFolder(intent.folderId)
            is DocumentsIntent.SelectDocument -> select(intent.documentId)
            DocumentsIntent.StartEditing -> startEditing()
            is DocumentsIntent.EditorChanged ->
                setState { copy(editorMarkdown = intent.markdown, draftMessage = null) }
            DocumentsIntent.SaveDraft -> persistDraft()
            is DocumentsIntent.Publish -> publish(intent.changeSummary)
            DocumentsIntent.CancelEditing ->
                setState { copy(isEditing = false, editorMarkdown = "", draftMessage = null) }
            DocumentsIntent.ShowHistory -> showHistory()
            DocumentsIntent.HideHistory -> setState { copy(isHistoryVisible = false) }
            is DocumentsIntent.RestoreVersion -> restore(intent.versionId)
            is DocumentsIntent.CreateDocument -> create(intent.title)
            DocumentsIntent.ToggleExplorerPanel -> setState { copy(isExplorerExpanded = !isExplorerExpanded) }
            DocumentsIntent.ToggleAttributesPanel -> setState { copy(isAttributesExpanded = !isAttributesExpanded) }
        }
    }

    private suspend fun initialize() {
        setState { copy(isLoading = true, role = roleProvider()) }
        val documents = getDocuments().getOrDefault(emptyList())
        val tree = getDocumentTree().getOrDefault(null)
        setState {
            copy(
                isLoading = false,
                documents = documents,
                tree = tree,
                expandedFolders = tree?.children?.map { it.id }?.toSet().orEmpty() + setOfNotNull(tree?.id),
            )
        }
    }

    private fun toggleFolder(folderId: String) {
        setState {
            copy(
                expandedFolders = if (folderId in expandedFolders) {
                    expandedFolders - folderId
                } else {
                    expandedFolders + folderId
                },
            )
        }
    }

    private fun select(documentId: String) {
        val document = currentState.documents.firstOrNull { it.id == documentId } ?: return
        val pathFolders = currentState.tree?.folderPathTo(documentId).orEmpty()
        setState {
            copy(
                selectedDocument = document,
                isEditing = false,
                editorMarkdown = "",
                isHistoryVisible = false,
                versions = emptyList(),
                errorMessage = null,
                expandedFolders = expandedFolders + pathFolders,
                isExplorerExpanded = false,
                isAttributesExpanded = false,
            )
        }
    }

    /** Ids of every folder on the path to [documentId], or `null` if not found under this node. */
    private fun DocumentNode.folderPathTo(documentId: String): List<String>? {
        if (this.documentId == documentId) return emptyList()
        children.forEach { child ->
            val path = child.folderPathTo(documentId)
            if (path != null) return listOf(id) + path
        }
        return null
    }

    private fun startEditing() {
        val document = currentState.selectedDocument ?: return
        if (!currentState.canEdit) {
            setState { copy(errorMessage = "Your role cannot edit documents") }
            return
        }
        setState { copy(isEditing = true, editorMarkdown = document.rawMarkdown, draftMessage = null) }
    }

    private suspend fun persistDraft() {
        val document = currentState.selectedDocument ?: return
        saveDraft(document.id, currentState.editorMarkdown)
            .onSuccess { receipt ->
                setState { copy(draftMessage = "Draft saved · ${receipt.contentHash.take(8)}") }
            }
            .onFailure { error -> setState { copy(errorMessage = error.message) } }
    }

    private suspend fun publish(changeSummary: String) {
        val document = currentState.selectedDocument ?: return
        publishDocument(currentState.role, document.id, currentState.editorMarkdown, changeSummary)
            .onSuccess { published ->
                refreshAfterMutation(selectedId = published.id)
                setState { copy(isEditing = false, editorMarkdown = "", draftMessage = null) }
                sendEffect(DocumentsEffect.ShowNotice("Published \"${published.title}\""))
            }
            .onFailure { error -> setState { copy(errorMessage = error.message) } }
    }

    private suspend fun showHistory() {
        val document = currentState.selectedDocument ?: return
        val versions = getVersions(document.id).getOrDefault(emptyList())
        setState { copy(isHistoryVisible = true, versions = versions) }
    }

    private suspend fun restore(versionId: String) {
        val document = currentState.selectedDocument ?: return
        restoreVersion(currentState.role, document.id, versionId)
            .onSuccess { restored ->
                refreshAfterMutation(selectedId = restored.id)
                setState { copy(isHistoryVisible = false) }
                sendEffect(DocumentsEffect.ShowNotice("Restored a previous version"))
            }
            .onFailure { error -> setState { copy(errorMessage = error.message) } }
    }

    private suspend fun create(title: String) {
        createDocument(currentState.role, title, "# $title\n\n")
            .onSuccess { created ->
                refreshAfterMutation(selectedId = created.id)
                setState { copy(isEditing = true, editorMarkdown = created.rawMarkdown) }
            }
            .onFailure { error -> setState { copy(errorMessage = error.message) } }
    }

    private suspend fun refreshAfterMutation(selectedId: String) {
        val documents = getDocuments().getOrDefault(currentState.documents)
        val tree = getDocumentTree().getOrDefault(currentState.tree)
        setState {
            copy(
                documents = documents,
                tree = tree,
                selectedDocument = documents.firstOrNull { it.id == selectedId } ?: selectedDocument,
            )
        }
    }
}
