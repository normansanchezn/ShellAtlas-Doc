package com.shelldocs.core.data.demo

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.domain.entity.source.KnowledgeSource
import com.shelldocs.core.domain.entity.source.SourceStatus
import com.shelldocs.core.domain.entity.source.SyncLogEntry
import com.shelldocs.core.domain.entity.source.SyncOutcome
import com.shelldocs.core.domain.repository.SourcesRepository

/** In-memory integrations with working sync/reconnect state transitions. */
class DemoSourcesRepository(private val timeProvider: TimeProvider) : SourcesRepository {

    private val sources = DemoSeed.sources.associateBy { it.id }.toMutableMap()
    private val log = DemoSeed.syncLog.toMutableList()
    private var logCounter = 0

    override suspend fun sources(): DomainResult<List<KnowledgeSource>> =
        DomainResult.success(sources.values.sortedBy { it.kind.ordinal })

    override suspend fun syncLog(): DomainResult<List<SyncLogEntry>> =
        DomainResult.success(log.sortedByDescending { it.occurredAt })

    override suspend fun sync(sourceId: String): DomainResult<KnowledgeSource> {
        val source = sources[sourceId]
            ?: return DomainResult.failure(AppError.NotFound("Unknown source"))
        if (source.status == SourceStatus.ERROR) {
            return DomainResult.failure(AppError.Conflict("Reconnect the integration before syncing"))
        }
        val synced = source.copy(lastSyncAt = timeProvider.now())
        sources[sourceId] = synced
        log += SyncLogEntry(
            id = "log-new-${++logCounter}",
            sourceKind = source.kind,
            outcome = SyncOutcome.SUCCESS,
            message = "${source.kind.displayName} sync completed",
            newDocs = 0,
            updatedDocs = 1,
            occurredAt = timeProvider.now(),
        )
        return DomainResult.success(synced)
    }

    override suspend fun reconnect(sourceId: String): DomainResult<KnowledgeSource> {
        val source = sources[sourceId]
            ?: return DomainResult.failure(AppError.NotFound("Unknown source"))
        val reconnected = source.copy(
            status = SourceStatus.CONNECTED,
            errorMessage = null,
            lastSyncAt = timeProvider.now(),
        )
        sources[sourceId] = reconnected
        return DomainResult.success(reconnected)
    }
}
