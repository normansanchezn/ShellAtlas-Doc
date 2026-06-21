@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.feature.documents.presentation

import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.error.toErrorDialogState
import com.shelldocs.core.common.mvi.MviViewModel
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.getOrDefault
import com.shelldocs.core.common.result.onFailure
import com.shelldocs.core.common.result.onSuccess
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.entity.document.DocumentAttributes
import com.shelldocs.core.domain.entity.document.DocumentNode
import com.shelldocs.core.domain.usecase.document.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DocumentsViewModel(
    private val getDocuments: GetDocumentsUseCase,
    private val getDocumentTree: GetDocumentTreeUseCase,
    private val saveDraft: SaveDraftUseCase,
    private val publishDocument: PublishDocumentUseCase,
    private val getVersions: GetDocumentVersionsUseCase,
    private val restoreVersion: RestoreDocumentVersionUseCase,
    private val createDocument: CreateDocumentUseCase,
    private val updateAttributes: UpdateDocumentAttributesUseCase,
    private val deleteDocument: DeleteDocumentUseCase,
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
            DocumentsIntent.ContinueToPreview -> openAttributesEditor(forPreview = true)
            DocumentsIntent.BackToEditor -> setState { copy(editorStep = DocumentsEditorStep.Edit) }
            DocumentsIntent.SaveDraft -> persistDraft()
            is DocumentsIntent.Publish -> publish(intent.changeSummary)
            DocumentsIntent.CancelEditing ->
                setState {
                    copy(
                        isEditing = false,
                        editorMarkdown = "",
                        editorStep = DocumentsEditorStep.Edit,
                        draftMessage = null,
                        isAttributesDialogOpen = false,
                        shouldShowPreviewAfterAttributes = false,
                    )
                }
            DocumentsIntent.ShowHistory -> showHistory()
            DocumentsIntent.HideHistory -> setState { copy(isHistoryVisible = false) }
            is DocumentsIntent.RestoreVersion -> restore(intent.versionId)
            DocumentsIntent.StartCreatingDocument -> startCreatingDocument()
            is DocumentsIntent.CreateDocument -> create(intent.title)
            is DocumentsIntent.NewDocumentTitleChanged ->
                setState { copy(newDocumentTitle = intent.value) }
            is DocumentsIntent.NewDocumentMarkdownChanged ->
                setState { copy(newDocumentMarkdown = intent.value) }
            DocumentsIntent.ContinueNewDocumentToPreview -> openNewDocumentAttributesEditor(forPreview = true)
            DocumentsIntent.BackToNewDocumentEditor -> setState { copy(newDocumentStep = DocumentsEditorStep.Edit) }
            DocumentsIntent.CancelNewDocument ->
                setState {
                    copy(
                        isCreatingDocument = false,
                        newDocumentTitle = "",
                        newDocumentMarkdown = "",
                        newDocumentStep = DocumentsEditorStep.Edit,
                        loadingMessage = null,
                        isAttributesDialogOpen = false,
                        shouldShowPreviewAfterAttributes = false,
                    )
                }
            DocumentsIntent.SubmitNewDocument -> submitNewDocument()
            DocumentsIntent.ToggleExplorerPanel -> setState { copy(isExplorerExpanded = !isExplorerExpanded) }
            DocumentsIntent.ToggleAttributesPanel -> setState { copy(isAttributesExpanded = !isAttributesExpanded) }
            DocumentsIntent.OpenAttributesEditor -> openAttributesEditor()
            DocumentsIntent.OpenNewDocumentAttributesEditor -> openNewDocumentAttributesEditor()
            DocumentsIntent.CloseAttributesEditor ->
                setState { copy(isAttributesDialogOpen = false, shouldShowPreviewAfterAttributes = false) }
            is DocumentsIntent.AttributesOwnerChanged ->
                setState { copy(attributesDraft = attributesDraft.copy(owner = intent.value)) }
            is DocumentsIntent.AttributesModuleChanged ->
                setState { copy(attributesDraft = attributesDraft.copy(module = intent.value)) }
            is DocumentsIntent.AttributesTeamChanged ->
                setState { copy(attributesDraft = attributesDraft.copy(team = intent.value)) }
            is DocumentsIntent.AttributesPlatformChanged ->
                setState { copy(attributesDraft = attributesDraft.copy(platform = intent.value)) }
            is DocumentsIntent.AttributesTagsChanged ->
                setState { copy(attributesDraft = attributesDraft.copy(tagsText = intent.value)) }
            is DocumentsIntent.AttributesAreaChanged ->
                setState { copy(attributesDraft = attributesDraft.copy(area = intent.value)) }

            is DocumentsIntent.AttributesVersionChanged ->
                setState { copy(attributesDraft = attributesDraft.copy(applicationVersion = intent.value)) }
            DocumentsIntent.SaveAttributes -> persistAttributes()
            DocumentsIntent.DismissError -> setState { copy(errorDialog = null) }
            is DocumentsIntent.ToggleBookmark -> toggleBookmark(intent.documentId)
            DocumentsIntent.ExportPdf -> exportPdf()
            is DocumentsIntent.BreadcrumbNavigate -> breadcrumbNavigate(intent.folderId)
            is DocumentsIntent.RequestDeleteDocument ->
                setState {
                    copy(
                        pendingDeleteDocumentId = intent.documentId,
                        pendingDeleteDocumentTitle = intent.documentTitle,
                    )
                }

            DocumentsIntent.CancelDeleteDocument ->
                setState { copy(pendingDeleteDocumentId = null, pendingDeleteDocumentTitle = "") }

            DocumentsIntent.ConfirmDeleteDocument -> confirmDeleteDocument()
        }
    }

    private suspend fun initialize() {
        setState { copy(isLoading = true, loadingMessage = "Loading documents...", role = roleProvider(), errorDialog = null) }
        val (documentsResult, treeResult) = coroutineScope {
            val documentsDeferred = async(dispatchers.io) { getDocuments() }
            val treeDeferred = async(dispatchers.default) { getDocumentTree() }
            documentsDeferred.await() to treeDeferred.await()
        }
        val documents = documentsResult.getOrDefault(emptyList())
        val tree = treeResult.getOrDefault(null)
        setState {
            copy(
                isLoading = false,
                loadingMessage = null,
                documents = documents,
                tree = tree,
                expandedFolders = tree?.allFolderIds().orEmpty(),
                errorDialog = when {
                    documentsResult is DomainResult.Failure ->
                        documentsResult.error.toErrorDialogState("load the documents")
                    treeResult is DomainResult.Failure ->
                        treeResult.error.toErrorDialogState("load the document folders")
                    else -> null
                },
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
                editorStep = DocumentsEditorStep.Edit,
                isHistoryVisible = false,
                versions = emptyList(),
                errorDialog = null,
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
            setState {
                copy(
                    errorDialog = com.shelldocs.core.common.error.AppError.Unauthorized()
                        .toErrorDialogState("open the editor"),
                )
            }
            return
        }
        setState {
            copy(
                isEditing = true,
                editorMarkdown = document.rawMarkdown,
                editorStep = DocumentsEditorStep.Edit,
                draftMessage = null,
                shouldShowPreviewAfterAttributes = false,
            )
        }
    }

    private fun startCreatingDocument() {
        if (!currentState.canEdit) {
            setState {
                copy(
                    errorDialog = com.shelldocs.core.common.error.AppError.Unauthorized()
                        .toErrorDialogState("create a document"),
                )
            }
            return
        }
        setState {
            copy(
                isCreatingDocument = true,
                isEditing = false,
                selectedDocument = null,
                newDocumentTitle = "",
                newDocumentMarkdown = "",
                newDocumentStep = DocumentsEditorStep.Edit,
                attributesDraft = AttributesDraft(),
                attributesEditorTarget = AttributesEditorTarget.NewDocument,
                errorDialog = null,
            )
        }
    }

    private suspend fun persistDraft() {
        val document = currentState.selectedDocument ?: return
        setState { copy(loadingMessage = "Saving draft...", errorDialog = null, draftMessage = null) }
        withContext(dispatchers.io) {
            saveDraft(document.id, currentState.editorMarkdown)
        }
            .onSuccess { receipt ->
                setState {
                    copy(
                        loadingMessage = null,
                        draftMessage = "Draft saved · ${receipt.contentHash.take(8)}",
                    )
                }
            }
            .onFailure { error ->
                setState {
                    copy(
                        loadingMessage = null,
                        errorDialog = error.toErrorDialogState("save your draft"),
                    )
                }
            }
    }

    private suspend fun publish(changeSummary: String) {
        val document = currentState.selectedDocument ?: return
        setState { copy(loadingMessage = "Publishing document...", errorDialog = null) }
        withContext(dispatchers.io) {
            publishDocument(currentState.role, document.id, currentState.editorMarkdown, changeSummary)
        }
            .onSuccess { published ->
                refreshAfterMutation(selectedId = published.id)
                setState {
                    copy(
                        loadingMessage = null,
                        isEditing = false,
                        editorMarkdown = "",
                        editorStep = DocumentsEditorStep.Edit,
                        draftMessage = null,
                        shouldShowPreviewAfterAttributes = false,
                    )
                }
                sendEffect(DocumentsEffect.ShowNotice("Published \"${published.title}\""))
            }
            .onFailure { error ->
                setState {
                    copy(
                        loadingMessage = null,
                        errorDialog = error.toErrorDialogState("publish this document"),
                    )
                }
            }
    }

    private suspend fun showHistory() {
        val document = currentState.selectedDocument ?: return
        setState { copy(loadingMessage = "Loading version history...", errorDialog = null) }
        withContext(dispatchers.default) {
            getVersions(document.id)
        }
            .onSuccess { versions ->
                setState { copy(loadingMessage = null, isHistoryVisible = true, versions = versions) }
            }
            .onFailure { error ->
                setState {
                    copy(
                        loadingMessage = null,
                        errorDialog = error.toErrorDialogState("load the version history"),
                    )
                }
            }
    }

    private suspend fun restore(versionId: String) {
        val document = currentState.selectedDocument ?: return
        setState { copy(loadingMessage = "Restoring version...", errorDialog = null) }
        withContext(dispatchers.io) {
            restoreVersion(currentState.role, document.id, versionId)
        }
            .onSuccess { restored ->
                refreshAfterMutation(selectedId = restored.id)
                setState { copy(loadingMessage = null, isHistoryVisible = false) }
                sendEffect(DocumentsEffect.ShowNotice("Restored a previous version"))
            }
            .onFailure { error ->
                setState {
                    copy(
                        loadingMessage = null,
                        errorDialog = error.toErrorDialogState("restore this version"),
                    )
                }
            }
    }

    private suspend fun create(title: String) {
        setState { copy(loadingMessage = "Creating document...", errorDialog = null) }
        withContext(dispatchers.io) {
            createDocument(currentState.role, title, "# $title\n\n")
        }
            .onSuccess { created ->
                refreshAfterMutation(selectedId = created.id)
                setState {
                    copy(
                        loadingMessage = null,
                        isEditing = true,
                        editorMarkdown = created.rawMarkdown,
                    )
                }
            }
            .onFailure { error ->
                setState {
                    copy(
                        loadingMessage = null,
                        errorDialog = error.toErrorDialogState("create a document"),
                    )
                }
            }
    }

    private suspend fun submitNewDocument() {
        val title = currentState.newDocumentTitle.trim()
        val markdown = currentState.newDocumentMarkdown.trim().ifBlank { buildMarkdownForTitle(title) }
        val attributesDraft = currentState.attributesDraft
        if (!attributesDraft.isComplete) {
            setState {
                copy(
                    isAttributesDialogOpen = true,
                    attributesEditorTarget = AttributesEditorTarget.NewDocument,
                    errorDialog = com.shelldocs.core.common.error.AppError.Validation(
                        "Pick an Area, Platform and Version before creating the document — that's how it gets filed correctly in Confluence.",
                    ).toErrorDialogState("create this document"),
                )
            }
            return
        }
        setState { copy(loadingMessage = "Creating document...", errorDialog = null) }
        withContext(dispatchers.io) {
            createDocument(currentState.role, title, markdown)
        }
            .onSuccess { created ->
                val updatedDocument = withContext(dispatchers.io) {
                    updateAttributes(
                        currentState.role,
                        created.id,
                        buildAttributesFromDraft(attributesDraft),
                    ).getOrDefault(created)
                }
                refreshAfterMutation(selectedId = created.id)
                setState {
                    copy(
                        loadingMessage = null,
                        isCreatingDocument = false,
                        newDocumentTitle = "",
                        newDocumentMarkdown = "",
                        newDocumentStep = DocumentsEditorStep.Edit,
                        attributesDraft = AttributesDraft(),
                        shouldShowPreviewAfterAttributes = false,
                        isEditing = true,
                        editorMarkdown = updatedDocument.rawMarkdown,
                        editorStep = DocumentsEditorStep.Edit,
                    )
                }
                sendEffect(DocumentsEffect.ShowNotice("Created \"${updatedDocument.title}\""))
            }
            .onFailure { error ->
                setState {
                    copy(
                        loadingMessage = null,
                        errorDialog = error.toErrorDialogState("create a document"),
                    )
                }
            }
    }

    private fun buildMarkdownForTitle(title: String): String = buildString {
        append("# ")
        append(title)
        append("\n\n")
    }

    private fun openAttributesEditor(forPreview: Boolean = false) {
        val document = currentState.selectedDocument ?: return
        setState {
            copy(
                isAttributesDialogOpen = true,
                shouldShowPreviewAfterAttributes = forPreview,
                attributesEditorTarget = AttributesEditorTarget.ExistingDocument,
                attributesDraft = AttributesDraft(
                    owner = document.attributes.owner,
                    module = document.attributes.module,
                    team = document.attributes.team,
                    platform = document.attributes.platform,
                    tagsText = document.attributes.tags.joinToString(", "),
                    area = document.attributes.area,
                    applicationVersion = document.attributes.applicationVersion,
                ),
            )
        }
    }

    private fun openNewDocumentAttributesEditor(forPreview: Boolean = false) {
        setState {
            copy(
                isAttributesDialogOpen = true,
                shouldShowPreviewAfterAttributes = forPreview,
                attributesEditorTarget = AttributesEditorTarget.NewDocument,
            )
        }
    }

    private suspend fun persistAttributes() {
        val draft = currentState.attributesDraft
        if (currentState.attributesEditorTarget == AttributesEditorTarget.NewDocument) {
            setState {
                copy(
                    isAttributesDialogOpen = false,
                    shouldShowPreviewAfterAttributes = false,
                    newDocumentStep = if (shouldShowPreviewAfterAttributes) {
                        DocumentsEditorStep.Preview
                    } else {
                        newDocumentStep
                    },
                )
            }
            return
        }

        val document = currentState.selectedDocument ?: return
        if (!draft.isComplete) {
            setState {
                copy(
                    errorDialog = com.shelldocs.core.common.error.AppError.Validation(
                        "Area, Platform and Version are required so this document files correctly in Confluence.",
                    ).toErrorDialogState("save the document details"),
                )
            }
            return
        }
        val attributes = buildAttributesFromDraft(draft, document.attributes.parentFolderId)
        setState { copy(loadingMessage = "Saving attributes...", errorDialog = null) }
        withContext(dispatchers.io) {
            updateAttributes(currentState.role, document.id, attributes)
        }
            .onSuccess { updated ->
                setState {
                    copy(
                        loadingMessage = null,
                        isAttributesDialogOpen = false,
                        shouldShowPreviewAfterAttributes = false,
                        editorStep = if (shouldShowPreviewAfterAttributes) {
                            DocumentsEditorStep.Preview
                        } else {
                            editorStep
                        },
                        selectedDocument = updated,
                        documents = documents.map { if (it.id == updated.id) updated else it },
                    )
                }
                sendEffect(DocumentsEffect.ShowNotice("Attributes updated"))
            }
            .onFailure { error ->
                setState {
                    copy(
                        loadingMessage = null,
                        errorDialog = error.toErrorDialogState("save the document details"),
                    )
                }
            }
    }

    private fun buildAttributesFromDraft(
        draft: AttributesDraft,
        parentFolderId: String? = null,
    ): DocumentAttributes = DocumentAttributes(
        owner = draft.owner,
        module = draft.module,
        team = draft.team,
        platform = draft.platform,
        parentFolderId = parentFolderId,
        tags = draft.tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        area = draft.area,
        applicationVersion = draft.applicationVersion,
    )

    private fun toggleBookmark(documentId: String) {
        setState {
            val updated = if (documentId in bookmarkedDocumentIds) {
                bookmarkedDocumentIds - documentId
            } else {
                bookmarkedDocumentIds + documentId
            }
            copy(bookmarkedDocumentIds = updated)
        }
    }

    private suspend fun exportPdf() {
        val document = currentState.selectedDocument ?: return
        sendEffect(DocumentsEffect.ExportDocumentAsPdf(document.id, document.title, document.rawMarkdown))
    }

    private fun breadcrumbNavigate(folderId: String?) {
        if (folderId == null) {
            setState {
                copy(
                    selectedDocument = null,
                    isEditing = false,
                    isHistoryVisible = false,
                    isExplorerExpanded = true,
                )
            }
        } else {
            setState { copy(expandedFolders = expandedFolders + folderId, isExplorerExpanded = true) }
        }
    }

    private fun DocumentNode.allFolderIds(): Set<String> {
        val ids = mutableSetOf<String>()
        fun collect(node: DocumentNode) {
            if (node.type == com.shelldocs.core.domain.entity.document.DocumentNodeType.FOLDER) {
                ids.add(node.id)
            }
            node.children.forEach { collect(it) }
        }
        collect(this)
        return ids
    }

    private suspend fun confirmDeleteDocument() {
        val documentId = currentState.pendingDeleteDocumentId ?: return
        setState { copy(isDeletingDocument = true, errorDialog = null) }
        withContext(dispatchers.io) {
            deleteDocument(currentState.role, documentId)
        }
            .onSuccess {
                val documents = withContext(dispatchers.io) {
                    getDocuments().getOrDefault(currentState.documents)
                }
                val tree = withContext(dispatchers.default) {
                    getDocumentTree().getOrDefault(currentState.tree)
                }
                setState {
                    copy(
                        documents = documents,
                        tree = tree,
                        selectedDocument = selectedDocument?.takeIf { it.id != documentId },
                        isEditing = if (selectedDocument?.id == documentId) false else isEditing,
                        isDeletingDocument = false,
                        pendingDeleteDocumentId = null,
                        pendingDeleteDocumentTitle = "",
                        bookmarkedDocumentIds = bookmarkedDocumentIds - documentId,
                    )
                }
                sendEffect(DocumentsEffect.ShowNotice("Document deleted"))
            }
            .onFailure { error ->
                setState {
                    copy(
                        isDeletingDocument = false,
                        errorDialog = error.toErrorDialogState("delete this document"),
                    )
                }
            }
    }

    private suspend fun refreshAfterMutation(selectedId: String) {
        val documents = withContext(dispatchers.io) {
            getDocuments().getOrDefault(currentState.documents)
        }
        val tree = withContext(dispatchers.default) {
            getDocumentTree().getOrDefault(currentState.tree)
        }
        setState {
            copy(
                documents = documents,
                tree = tree,
                selectedDocument = documents.firstOrNull { it.id == selectedId } ?: selectedDocument,
            )
        }
    }
}
