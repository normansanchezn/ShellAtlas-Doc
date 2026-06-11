package com.shelldocs.core.domain.fixtures

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.DocumentVersion
import com.shelldocs.core.domain.entity.document.DraftReceipt
import com.shelldocs.core.domain.repository.DocumentRepository

class FakeDocumentRepository(
    var stored: List<Document> = emptyList(),
) : DocumentRepository {

    var deletedIds = mutableListOf<String>()
    var publishedIds = mutableListOf<String>()

    override suspend fun documents(): DomainResult<List<Document>> = DomainResult.success(stored)

    override suspend fun document(id: String): DomainResult<Document> =
        stored.firstOrNull { it.id == id }
            ?.let { DomainResult.success(it) }
            ?: DomainResult.failure(AppError.NotFound())

    override suspend fun search(query: String): DomainResult<List<Document>> =
        DomainResult.success(stored.filter { it.title.contains(query, ignoreCase = true) })

    override suspend fun create(
        title: String,
        markdown: String,
        parentFolderId: String?,
    ): DomainResult<Document> {
        val document = DocumentFixtures.document(id = "created", title = title, markdown = markdown)
        stored = stored + document
        return DomainResult.success(document)
    }

    override suspend fun publish(
        id: String,
        markdown: String,
        changeSummary: String,
    ): DomainResult<Document> {
        publishedIds += id
        return document(id)
    }

    override suspend fun saveDraft(id: String, markdown: String): DomainResult<DraftReceipt> =
        DomainResult.success(DraftReceipt(id, contentHash = markdown.hashCode().toString(), savedAt = DocumentFixtures.baseInstant))

    override suspend fun versions(id: String): DomainResult<List<DocumentVersion>> =
        DomainResult.success(emptyList())

    override suspend fun restoreVersion(id: String, versionId: String): DomainResult<Document> =
        document(id)

    override suspend fun delete(id: String): DomainResult<Unit> {
        deletedIds += id
        return DomainResult.success(Unit)
    }
}
