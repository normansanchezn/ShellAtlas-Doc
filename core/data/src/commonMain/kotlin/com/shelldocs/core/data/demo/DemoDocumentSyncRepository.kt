package com.shelldocs.core.data.demo

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.repository.DocumentSyncRepository
import kotlinx.coroutines.delay

/**
 * Local stand-in for the real Confluence / Azure DevOps Wiki / Search index
 * sync clients: succeeds after a short simulated round trip so the UI's
 * per-step states (Syncing Confluence..., Reindexing Search...) are visible.
 */
class DemoDocumentSyncRepository(private val simulatedLatencyMs: Long = 300) : DocumentSyncRepository {

    override suspend fun syncConfluence(documentId: String): DomainResult<Unit> {
        delay(simulatedLatencyMs)
        return DomainResult.success(Unit)
    }

    override suspend fun syncAzureDevOpsWiki(documentId: String): DomainResult<Unit> {
        delay(simulatedLatencyMs)
        return DomainResult.success(Unit)
    }

    override suspend fun reindexSearch(documentId: String): DomainResult<Unit> {
        delay(simulatedLatencyMs)
        return DomainResult.success(Unit)
    }
}
