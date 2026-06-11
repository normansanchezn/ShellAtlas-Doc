package com.shelldocs.core.domain.repository

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.DocumentVersion
import com.shelldocs.core.domain.entity.document.DraftReceipt

/**
 * CRUD + versioning over the documents API
 * (mirrors the original ShellDoc `/v1/documents` contract).
 */
interface DocumentRepository {

    suspend fun documents(): DomainResult<List<Document>>

    suspend fun document(id: String): DomainResult<Document>

    suspend fun search(query: String): DomainResult<List<Document>>

    suspend fun create(title: String, markdown: String, parentFolderId: String?): DomainResult<Document>

    suspend fun publish(id: String, markdown: String, changeSummary: String): DomainResult<Document>

    suspend fun saveDraft(id: String, markdown: String): DomainResult<DraftReceipt>

    suspend fun versions(id: String): DomainResult<List<DocumentVersion>>

    suspend fun restoreVersion(id: String, versionId: String): DomainResult<Document>

    suspend fun delete(id: String): DomainResult<Unit>
}
