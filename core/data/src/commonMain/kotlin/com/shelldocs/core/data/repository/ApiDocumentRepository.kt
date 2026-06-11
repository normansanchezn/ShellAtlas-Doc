package com.shelldocs.core.data.repository

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.data.mapper.DocumentDtoMapper
import com.shelldocs.core.data.mapper.DocumentVersionDtoMapper
import com.shelldocs.core.data.network.ShellDocsApi
import com.shelldocs.core.data.network.ShellDocsApiException
import com.shelldocs.core.data.network.dto.CreateDocumentRequestDto
import com.shelldocs.core.data.network.dto.PublishDocumentRequestDto
import com.shelldocs.core.data.network.dto.SaveDraftRequestDto
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.DocumentVersion
import com.shelldocs.core.domain.entity.document.DraftReceipt
import com.shelldocs.core.domain.repository.DocumentRepository
import kotlin.time.ExperimentalTime

/** [DocumentRepository] backed by the ShellDocs `/v1` backend. */
class ApiDocumentRepository(private val api: ShellDocsApi) : DocumentRepository {

    override suspend fun documents(): DomainResult<List<Document>> = guard {
        api.documents().map(DocumentDtoMapper::toDomain)
    }

    override suspend fun document(id: String): DomainResult<Document> = guard {
        DocumentDtoMapper.toDomain(api.document(id))
    }

    override suspend fun search(query: String): DomainResult<List<Document>> = guard {
        api.search(query).map(DocumentDtoMapper::toDomain)
    }

    override suspend fun create(
        title: String,
        markdown: String,
        parentFolderId: String?,
    ): DomainResult<Document> = guard {
        DocumentDtoMapper.toDomain(
            api.create(CreateDocumentRequestDto(title, markdown, parentFolderId)),
        )
    }

    override suspend fun publish(
        id: String,
        markdown: String,
        changeSummary: String,
    ): DomainResult<Document> = guard {
        DocumentDtoMapper.toDomain(api.publish(id, PublishDocumentRequestDto(markdown, changeSummary)))
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun saveDraft(id: String, markdown: String): DomainResult<DraftReceipt> = guard {
        val receipt = api.saveDraft(id, SaveDraftRequestDto(markdown))
        DraftReceipt(
            documentId = receipt.documentId,
            contentHash = receipt.contentHash,
            savedAt = runCatching { kotlin.time.Instant.parse(receipt.updatedAt) }
                .getOrElse { kotlin.time.Instant.fromEpochMilliseconds(0) },
        )
    }

    override suspend fun versions(id: String): DomainResult<List<DocumentVersion>> = guard {
        api.versions(id).map(DocumentVersionDtoMapper::toDomain)
    }

    override suspend fun restoreVersion(id: String, versionId: String): DomainResult<Document> = guard {
        DocumentDtoMapper.toDomain(api.restore(id, versionId))
    }

    override suspend fun delete(id: String): DomainResult<Unit> = guard { api.delete(id) }

    private inline fun <T> guard(block: () -> T): DomainResult<T> = try {
        DomainResult.success(block())
    } catch (exception: ShellDocsApiException) {
        DomainResult.failure(
            when (exception) {
                is ShellDocsApiException.NotFound -> AppError.NotFound()
                is ShellDocsApiException.Unauthorized -> AppError.Unauthorized()
                is ShellDocsApiException.Http -> AppError.Unknown("HTTP ${exception.statusCode}")
            },
        )
    } catch (exception: Exception) {
        DomainResult.failure(AppError.Network(exception.message ?: "Network failure"))
    }
}
