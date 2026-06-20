package com.shelldocs.core.domain.repository

import com.shelldocs.core.common.result.DomainResult

/**
 * Sources-of-truth synchronization fan-out for a published document update.
 * Each target is invoked independently so the UI can show per-step progress
 * (Syncing Confluence... / Updating Azure DevOps... / Reindexing Search...).
 */
interface DocumentSyncRepository {

    suspend fun syncConfluence(documentId: String): DomainResult<Unit>

    suspend fun syncAzureDevOpsWiki(documentId: String): DomainResult<Unit>

    suspend fun reindexSearch(documentId: String): DomainResult<Unit>
}
