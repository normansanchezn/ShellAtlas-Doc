package com.shelldocs.core.data.repository

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.onSuccess
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.DocumentVersion
import com.shelldocs.core.domain.entity.document.DraftReceipt
import com.shelldocs.core.domain.repository.DocumentRepository

/**
 * In-memory read-through cache decorator. Mutations invalidate the cache so
 * readers never observe stale lists after a publish/delete.
 */
class CachingDocumentRepository(private val delegate: DocumentRepository) : DocumentRepository {

    private var cachedDocuments: List<Document>? = null

    override suspend fun documents(): DomainResult<List<Document>> {
        cachedDocuments?.let { return DomainResult.success(it) }
        return delegate.documents().onSuccess { cachedDocuments = it }
    }

    override suspend fun document(id: String): DomainResult<Document> {
        cachedDocuments?.firstOrNull { it.id == id }?.let { return DomainResult.success(it) }
        return delegate.document(id)
    }

    override suspend fun search(query: String): DomainResult<List<Document>> = delegate.search(query)

    override suspend fun create(
        title: String,
        markdown: String,
        parentFolderId: String?,
    ): DomainResult<Document> =
        delegate.create(title, markdown, parentFolderId).onSuccess { invalidate() }

    override suspend fun publish(
        id: String,
        markdown: String,
        changeSummary: String,
    ): DomainResult<Document> =
        delegate.publish(id, markdown, changeSummary).onSuccess { invalidate() }

    override suspend fun saveDraft(id: String, markdown: String): DomainResult<DraftReceipt> =
        delegate.saveDraft(id, markdown)

    override suspend fun versions(id: String): DomainResult<List<DocumentVersion>> =
        delegate.versions(id)

    override suspend fun restoreVersion(id: String, versionId: String): DomainResult<Document> =
        delegate.restoreVersion(id, versionId).onSuccess { invalidate() }

    override suspend fun delete(id: String): DomainResult<Unit> =
        delegate.delete(id).onSuccess { invalidate() }

    fun invalidate() {
        cachedDocuments = null
    }
}
