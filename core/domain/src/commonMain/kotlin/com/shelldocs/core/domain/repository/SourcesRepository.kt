package com.shelldocs.core.domain.repository

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.source.KnowledgeSource
import com.shelldocs.core.domain.entity.source.SyncLogEntry

/** External integrations (Confluence, Azure DevOps, Jira) and their sync runs. */
interface SourcesRepository {

    suspend fun sources(): DomainResult<List<KnowledgeSource>>

    suspend fun syncLog(): DomainResult<List<SyncLogEntry>>

    suspend fun sync(sourceId: String): DomainResult<KnowledgeSource>

    suspend fun reconnect(sourceId: String): DomainResult<KnowledgeSource>
}
