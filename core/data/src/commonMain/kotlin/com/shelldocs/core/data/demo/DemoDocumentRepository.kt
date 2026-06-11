package com.shelldocs.core.data.demo

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.data.markdown.MarkdownParser
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.DocumentAttributes
import com.shelldocs.core.domain.entity.document.DocumentClassification
import com.shelldocs.core.domain.entity.document.DocumentStatus
import com.shelldocs.core.domain.entity.document.DocumentVersion
import com.shelldocs.core.domain.entity.document.DraftReceipt
import com.shelldocs.core.domain.repository.DocumentRepository
import kotlin.time.ExperimentalTime

/**
 * Fully functional in-memory [DocumentRepository] with versioning, used in
 * demo mode and as a reference implementation for the API contract.
 */
@OptIn(ExperimentalTime::class)
class DemoDocumentRepository(
    private val timeProvider: TimeProvider,
    private val markdownParser: MarkdownParser = MarkdownParser(),
    seed: List<Document> = DemoSeed.documents,
) : DocumentRepository {

    private val documents = seed.associateBy { it.id }.toMutableMap()
    private val versionHistory = mutableMapOf<String, MutableList<DocumentVersion>>()
    private var idCounter = 0

    init {
        seed.forEach { document ->
            versionHistory[document.id] = mutableListOf(
                DocumentVersion(
                    id = "${document.id}-v1",
                    documentId = document.id,
                    versionNumber = 1,
                    title = document.title,
                    rawMarkdown = document.rawMarkdown,
                    changeSummary = "Initial import",
                    createdAt = document.createdAt,
                ),
            )
        }
    }

    override suspend fun documents(): DomainResult<List<Document>> =
        DomainResult.success(documents.values.sortedBy { it.title.lowercase() })

    override suspend fun document(id: String): DomainResult<Document> =
        documents[id]?.let { DomainResult.success(it) }
            ?: DomainResult.failure(AppError.NotFound("Document $id not found"))

    override suspend fun search(query: String): DomainResult<List<Document>> {
        val needle = query.lowercase()
        return DomainResult.success(
            documents.values.filter { document ->
                needle in document.title.lowercase() ||
                    needle in document.plainText.lowercase() ||
                    document.attributes.tags.any { needle in it.lowercase() }
            }.sortedBy { it.title.lowercase() },
        )
    }

    override suspend fun create(
        title: String,
        markdown: String,
        parentFolderId: String?,
    ): DomainResult<Document> {
        val parsed = markdownParser.parse(markdown)
        val id = "doc-new-${++idCounter}"
        val now = timeProvider.now()
        val document = Document(
            id = id,
            title = title,
            summary = "",
            status = DocumentStatus.DRAFT,
            classification = DocumentClassification.INTERNAL,
            rawMarkdown = markdown,
            content = parsed.content,
            plainText = parsed.plainText,
            attributes = DocumentAttributes(parentFolderId = parentFolderId, platform = "General"),
            createdAt = now,
            updatedAt = now,
        )
        documents[id] = document
        versionHistory[id] = mutableListOf(
            DocumentVersion(id = "$id-v1", documentId = id, versionNumber = 1, title = title, rawMarkdown = markdown, changeSummary = "Created", createdAt = now),
        )
        return DomainResult.success(document)
    }

    override suspend fun publish(
        id: String,
        markdown: String,
        changeSummary: String,
    ): DomainResult<Document> {
        val existing = documents[id]
            ?: return DomainResult.failure(AppError.NotFound("Document $id not found"))
        val parsed = markdownParser.parse(markdown)
        val now = timeProvider.now()
        val history = versionHistory.getValue(id)
        history += DocumentVersion(
            id = "$id-v${history.size + 1}",
            documentId = id,
            versionNumber = history.size + 1,
            title = existing.title,
            rawMarkdown = markdown,
            changeSummary = changeSummary,
            createdAt = now,
        )
        val updated = existing.copy(
            rawMarkdown = markdown,
            content = parsed.content,
            plainText = parsed.plainText,
            status = DocumentStatus.PUBLISHED,
            updatedAt = now,
        )
        documents[id] = updated
        return DomainResult.success(updated)
    }

    override suspend fun saveDraft(id: String, markdown: String): DomainResult<DraftReceipt> {
        if (id !in documents) return DomainResult.failure(AppError.NotFound("Document $id not found"))
        val parsed = markdownParser.parse(markdown)
        return DomainResult.success(
            DraftReceipt(documentId = id, contentHash = parsed.contentHash, savedAt = timeProvider.now()),
        )
    }

    override suspend fun versions(id: String): DomainResult<List<DocumentVersion>> =
        versionHistory[id]?.let { DomainResult.success(it.sortedByDescending { version -> version.versionNumber }) }
            ?: DomainResult.failure(AppError.NotFound("Document $id not found"))

    override suspend fun restoreVersion(id: String, versionId: String): DomainResult<Document> {
        val version = versionHistory[id]?.firstOrNull { it.id == versionId }
            ?: return DomainResult.failure(AppError.NotFound("Version $versionId not found"))
        return publish(id, version.rawMarkdown, "Restored version ${version.versionNumber}")
    }

    override suspend fun delete(id: String): DomainResult<Unit> {
        documents.remove(id)
            ?: return DomainResult.failure(AppError.NotFound("Document $id not found"))
        versionHistory.remove(id)
        return DomainResult.success(Unit)
    }
}
