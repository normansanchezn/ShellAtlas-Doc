@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.feature.documents.presentation

import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.DocumentAttributes
import com.shelldocs.core.domain.entity.document.DocumentClassification
import com.shelldocs.core.domain.entity.document.DocumentContent
import com.shelldocs.core.domain.entity.document.DocumentNode
import com.shelldocs.core.domain.entity.document.DocumentNodeType
import com.shelldocs.core.domain.entity.document.DocumentStatus
import com.shelldocs.core.domain.entity.document.DocumentVersion
import com.shelldocs.core.domain.entity.document.DraftReceipt
import com.shelldocs.core.domain.repository.DocumentRepository
import com.shelldocs.core.domain.repository.DocumentTreeRepository
import com.shelldocs.core.domain.usecase.document.CreateDocumentUseCase
import com.shelldocs.core.domain.usecase.document.GetDocumentTreeUseCase
import com.shelldocs.core.domain.usecase.document.GetDocumentVersionsUseCase
import com.shelldocs.core.domain.usecase.document.GetDocumentsUseCase
import com.shelldocs.core.domain.usecase.document.PublishDocumentUseCase
import com.shelldocs.core.domain.usecase.document.RestoreDocumentVersionUseCase
import com.shelldocs.core.domain.usecase.document.SaveDraftUseCase
import com.shelldocs.core.domain.usecase.document.UpdateDocumentAttributesUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class SingleDispatcher(dispatcher: CoroutineDispatcher) : DispatcherProvider {
    override val main = dispatcher
    override val default = dispatcher
    override val io = dispatcher
}

private fun document(id: String, title: String) = Document(
    id = id,
    title = title,
    summary = "Summary of $title",
    status = DocumentStatus.PUBLISHED,
    classification = DocumentClassification.INTERNAL,
    rawMarkdown = "# $title\n\nBody.",
    content = DocumentContent(),
    plainText = "$title Body.",
    attributes = DocumentAttributes(owner = "Elena Vargas", platform = "iOS"),
    createdAt = Instant.parse("2026-06-01T00:00:00Z"),
    updatedAt = Instant.parse("2026-06-01T00:00:00Z"),
)

private class FakeDocsRepository : DocumentRepository {
    val docs = mutableMapOf(
        "doc-1" to document("doc-1", "Authentication"),
        "doc-2" to document("doc-2", "Release Process"),
    )
    var published = mutableListOf<String>()

    override suspend fun documents() = DomainResult.success(docs.values.toList())
    override suspend fun document(id: String) =
        docs[id]?.let { DomainResult.success(it) } ?: DomainResult.failure(AppError.NotFound())
    override suspend fun search(query: String) =
        DomainResult.success(docs.values.filter { it.title.contains(query, true) })
    override suspend fun create(title: String, markdown: String, parentFolderId: String?): DomainResult<Document> {
        val created = document("doc-created", title).copy(rawMarkdown = markdown)
        docs[created.id] = created
        return DomainResult.success(created)
    }
    override suspend fun publish(id: String, markdown: String, changeSummary: String): DomainResult<Document> {
        published += id
        val updated = docs.getValue(id).copy(rawMarkdown = markdown)
        docs[id] = updated
        return DomainResult.success(updated)
    }
    override suspend fun saveDraft(id: String, markdown: String) =
        DomainResult.success(DraftReceipt(id, "abcdef1234567890", Instant.parse("2026-06-11T00:00:00Z")))
    override suspend fun versions(id: String) = DomainResult.success(
        listOf(
            DocumentVersion("$id-v2", id, 2, "t", "# v2", "Edited", Instant.parse("2026-06-02T00:00:00Z")),
            DocumentVersion("$id-v1", id, 1, "t", "# v1", "Initial import", Instant.parse("2026-06-01T00:00:00Z")),
        ),
    )
    override suspend fun restoreVersion(id: String, versionId: String) = publish(id, "# restored", "Restored")
    override suspend fun updateAttributes(id: String, attributes: DocumentAttributes): DomainResult<Document> {
        val updated = docs.getValue(id).copy(attributes = attributes)
        docs[id] = updated
        return DomainResult.success(updated)
    }
    override suspend fun delete(id: String) = DomainResult.success(Unit)
}

private class FakeTreeRepository(private val docsRepository: FakeDocsRepository) : DocumentTreeRepository {
    override suspend fun tree(): DomainResult<DocumentNode> = DomainResult.success(
        DocumentNode(
            id = "root",
            title = "Root",
            type = DocumentNodeType.FOLDER,
            children = docsRepository.docs.values.map {
                DocumentNode("node-${it.id}", it.title, DocumentNodeType.DOCUMENT, documentId = it.id)
            },
        ),
    )
}

class DocumentsViewModelTest {

    private val repository = FakeDocsRepository()
    private var role = UserRole.OWNER

    private fun viewModel(scheduler: TestCoroutineScheduler) = DocumentsViewModel(
        getDocuments = GetDocumentsUseCase(repository),
        getDocumentTree = GetDocumentTreeUseCase(FakeTreeRepository(repository)),
        saveDraft = SaveDraftUseCase(repository),
        publishDocument = PublishDocumentUseCase(repository),
        getVersions = GetDocumentVersionsUseCase(repository),
        restoreVersion = RestoreDocumentVersionUseCase(repository),
        createDocument = CreateDocumentUseCase(repository),
        updateAttributes = UpdateDocumentAttributesUseCase(repository),
        roleProvider = { role },
        dispatchers = SingleDispatcher(StandardTestDispatcher(scheduler)),
    )

    @Test
    fun initializeLoadsDocumentsTreeAndExpandsRoot() = runTest {
        val viewModel = viewModel(testScheduler)

        viewModel.onIntent(DocumentsIntent.Initialize)
        testScheduler.advanceUntilIdle()

        val state = viewModel.currentState
        assertEquals(2, state.documents.size)
        assertNotNull(state.tree)
        assertTrue("root" in state.expandedFolders)
        assertFalse(state.isLoading)
        viewModel.clear()
    }

    @Test
    fun filterNarrowsTheList() = runTest {
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(DocumentsIntent.Initialize)
        viewModel.onIntent(DocumentsIntent.FilterChanged("release"))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("doc-2"), viewModel.currentState.filteredDocuments.map { it.id })
        viewModel.clear()
    }

    @Test
    fun editFlowSavesDraftAndPublishes() = runTest {
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(DocumentsIntent.Initialize)
        viewModel.onIntent(DocumentsIntent.SelectDocument("doc-1"))
        viewModel.onIntent(DocumentsIntent.StartEditing)
        viewModel.onIntent(DocumentsIntent.EditorChanged("# Authentication\n\nNew body."))
        viewModel.onIntent(DocumentsIntent.SaveDraft)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.currentState.draftMessage.orEmpty().startsWith("Draft saved"))

        viewModel.onIntent(DocumentsIntent.Publish("Refresh"))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("doc-1"), repository.published)
        assertFalse(viewModel.currentState.isEditing)
        assertTrue(viewModel.currentState.selectedDocument?.rawMarkdown?.contains("New body") == true)
        viewModel.clear()
    }

    @Test
    fun viewerRoleCannotStartEditing() = runTest {
        role = UserRole.VIEWER
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(DocumentsIntent.Initialize)
        viewModel.onIntent(DocumentsIntent.SelectDocument("doc-1"))
        viewModel.onIntent(DocumentsIntent.StartEditing)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.currentState.isEditing)
        assertNotNull(viewModel.currentState.errorDialog)
        viewModel.clear()
    }

    @Test
    fun historyLoadsVersionsAndRestorePublishes() = runTest {
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(DocumentsIntent.Initialize)
        viewModel.onIntent(DocumentsIntent.SelectDocument("doc-1"))
        viewModel.onIntent(DocumentsIntent.ShowHistory)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.currentState.isHistoryVisible)
        assertEquals(2, viewModel.currentState.versions.size)

        viewModel.onIntent(DocumentsIntent.RestoreVersion("doc-1-v1"))
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.currentState.isHistoryVisible)
        assertEquals(listOf("doc-1"), repository.published)
        viewModel.clear()
    }

    @Test
    fun togglingFoldersUpdatesExpansionSet() = runTest {
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(DocumentsIntent.Initialize)
        testScheduler.advanceUntilIdle()
        assertTrue("root" in viewModel.currentState.expandedFolders)

        viewModel.onIntent(DocumentsIntent.ToggleFolder("root"))
        testScheduler.advanceUntilIdle()
        assertFalse("root" in viewModel.currentState.expandedFolders)
        viewModel.clear()
    }

    @Test
    fun submitNewDocumentUsesTypedTitle() = runTest {
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(DocumentsIntent.Initialize)
        testScheduler.advanceUntilIdle()
        viewModel.onIntent(DocumentsIntent.StartCreatingDocument)
        testScheduler.advanceUntilIdle()
        viewModel.onIntent(DocumentsIntent.NewDocumentTitleChanged("Project playbook"))
        testScheduler.advanceUntilIdle()
        viewModel.onIntent(DocumentsIntent.SubmitNewDocument)
        testScheduler.advanceUntilIdle()

        val created = repository.docs.getValue("doc-created")
        assertEquals("Project playbook", created.title)
        assertTrue(created.rawMarkdown.startsWith("# Project playbook"))
        viewModel.clear()
    }

    @Test
    fun newDocumentWithBlankTitleIsRejected() = runTest {
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(DocumentsIntent.Initialize)
        viewModel.onIntent(DocumentsIntent.StartCreatingDocument)
        viewModel.onIntent(DocumentsIntent.NewDocumentTitleChanged("   "))
        viewModel.onIntent(DocumentsIntent.SubmitNewDocument)
        testScheduler.advanceUntilIdle()

        assertNotNull(viewModel.currentState.errorDialog)
        assertFalse("doc-created" in repository.docs)
        viewModel.clear()
    }

    @Test
    fun dismissErrorClearsDialog() = runTest {
        role = UserRole.VIEWER
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(DocumentsIntent.Initialize)
        viewModel.onIntent(DocumentsIntent.SelectDocument("doc-1"))
        viewModel.onIntent(DocumentsIntent.StartEditing)
        testScheduler.advanceUntilIdle()

        assertNotNull(viewModel.currentState.errorDialog)

        viewModel.onIntent(DocumentsIntent.DismissError)
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.currentState.errorDialog)
        viewModel.clear()
    }
}
