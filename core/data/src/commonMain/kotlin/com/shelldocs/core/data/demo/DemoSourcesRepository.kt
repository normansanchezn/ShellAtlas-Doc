package com.shelldocs.core.data.demo

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.logging.AppLogger
import com.shelldocs.core.common.logging.LogTags
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.domain.entity.source.*
import com.shelldocs.core.domain.repository.SourcesRepository
import kotlin.time.ExperimentalTime

/** In-memory integrations with working sync/reconnect state transitions. */
class DemoSourcesRepository(private val timeProvider: TimeProvider) : SourcesRepository {

    private val logger = AppLogger.tag(LogTags.INTEGRATION)
    private val sources = DemoSeed.sources.associateBy { it.id }.toMutableMap()
    private val log = DemoSeed.syncLog.toMutableList()
    private var logCounter = 0

    override suspend fun sources(): DomainResult<List<KnowledgeSource>> {
        val visible = sources.values
            .filter { it.kind != SourceKind.JIRA && it.kind != SourceKind.AZURE_DEVOPS }
            .sortedBy { it.kind.ordinal }
        visible.forEach { logger.i("${it.kind.displayName} status=${it.status} host=${it.host}") }
        SourceKind.entries
            .filter { it == SourceKind.JIRA || it == SourceKind.AZURE_DEVOPS }
            .forEach { logger.w("${it.displayName} integration not yet implemented — skipped") }
        return DomainResult.success(visible)
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun syncLog(): DomainResult<List<SyncLogEntry>> =
        DomainResult.success(log.sortedByDescending { it.occurredAt })

    @OptIn(ExperimentalTime::class)
    override suspend fun sync(sourceId: String): DomainResult<KnowledgeSource> {
        val source = sources[sourceId]
            ?: return DomainResult.failure(AppError.NotFound("Unknown source")).also {
                logger.e("sync($sourceId) failed: unknown source")
            }
        logger.i("sync(${source.kind.displayName}, $sourceId) starting")
        if (source.status == SourceStatus.ERROR) {
            logger.w("sync(${source.kind.displayName}, $sourceId) blocked: integration needs reconnect")
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
        logger.i("sync(${source.kind.displayName}, $sourceId) succeeded")
        return DomainResult.success(synced)
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun reconnect(sourceId: String): DomainResult<KnowledgeSource> {
        val source = sources[sourceId]
            ?: return DomainResult.failure(AppError.NotFound("Unknown source")).also {
                logger.e("reconnect($sourceId) failed: unknown source")
            }
        logger.i("reconnect(${source.kind.displayName}, $sourceId) starting")
        val reconnected = source.copy(
            status = SourceStatus.CONNECTED,
            errorMessage = null,
            lastSyncAt = timeProvider.now(),
        )
        sources[sourceId] = reconnected
        logger.i("reconnect(${source.kind.displayName}, $sourceId) succeeded")
        return DomainResult.success(reconnected)
    }
}
