package com.shelldocs.core.domain.usecase.document

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.repository.DocumentSyncRepository

/** Thin pass-through so each sync target stays independently callable/awaitable by the UI. */
class SyncDocumentToSourcesOfTruthUseCase(private val documentSyncRepository: DocumentSyncRepository) {

    suspend fun confluence(documentId: String): DomainResult<Unit> = documentSyncRepository.syncConfluence(documentId)

    suspend fun azureDevOpsWiki(documentId: String): DomainResult<Unit> =
        documentSyncRepository.syncAzureDevOpsWiki(documentId)

    suspend fun reindexSearch(documentId: String): DomainResult<Unit> = documentSyncRepository.reindexSearch(documentId)
}
