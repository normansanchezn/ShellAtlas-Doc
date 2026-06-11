package com.shelldocs.feature.sources.presentation

import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.entity.source.KnowledgeSource
import com.shelldocs.core.domain.entity.source.SourceKind
import com.shelldocs.core.domain.entity.source.SourceStatus
import com.shelldocs.core.domain.entity.source.SyncLogEntry
import com.shelldocs.core.domain.repository.SourcesRepository
import com.shelldocs.core.domain.usecase.source.GetSourcesUseCase
import com.shelldocs.core.domain.usecase.source.GetSyncLogUseCase
import com.shelldocs.core.domain.usecase.source.SyncSourceUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private class SingleDispatcher(dispatcher: CoroutineDispatcher) : DispatcherProvider {
    override val main = dispatcher
    override val default = dispatcher
    override val io = dispatcher
}

private class FakeSourcesRepository : SourcesRepository {
    private val now = Instant.parse("2026-06-11T10:00:00Z")
    private var jira = KnowledgeSource(
        id = "jira",
        kind = SourceKind.JIRA,
        host = "shell-jira.atlassian.net",
        status = SourceStatus.ERROR,
        importedDocs = 22,
        lastSyncAt = null,
        errorMessage = "Token expired",
    )
    private var confluence = KnowledgeSource(
        id = "confluence",
        kind = SourceKind.CONFLUENCE,
        host = "shell-engineering.atlassian.net",
        status = SourceStatus.CONNECTED,
        importedDocs = 84,
        lastSyncAt = now,
    )
    var syncs = 0

    override suspend fun sources() = DomainResult.success(listOf(confluence, jira))
    override suspend fun syncLog(): DomainResult<List<SyncLogEntry>> = DomainResult.success(emptyList())
    override suspend fun sync(sourceId: String): DomainResult<KnowledgeSource> {
        if (sourceId == "jira" && jira.status == SourceStatus.ERROR) {
            return DomainResult.failure(AppError.Conflict("Reconnect first"))
        }
        syncs++
        return DomainResult.success(confluence)
    }
    override suspend fun reconnect(sourceId: String): DomainResult<KnowledgeSource> {
        jira = jira.copy(status = SourceStatus.CONNECTED, errorMessage = null, lastSyncAt = now)
        return DomainResult.success(jira)
    }
}

class SourcesViewModelTest {

    private val repository = FakeSourcesRepository()
    private var role = UserRole.OWNER

    private fun viewModel(scheduler: kotlinx.coroutines.test.TestCoroutineScheduler) = SourcesViewModel(
        getSources = GetSourcesUseCase(repository),
        getSyncLog = GetSyncLogUseCase(repository),
        syncSource = SyncSourceUseCase(repository),
        sourcesRepository = repository,
        roleProvider = { role },
        dispatchers = SingleDispatcher(StandardTestDispatcher(scheduler)),
    )

    @Test
    fun initializeComputesAggregates() = runTest {
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(SourcesIntent.Initialize)
        testScheduler.advanceUntilIdle()

        assertEquals(106, viewModel.currentState.totalImportedDocs)
        assertEquals(1, viewModel.currentState.activeIntegrations)
        viewModel.clear()
    }

    @Test
    fun businessRoleCannotSync() = runTest {
        role = UserRole.BUSINESS
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(SourcesIntent.Initialize)
        viewModel.onIntent(SourcesIntent.Sync("confluence"))
        testScheduler.advanceUntilIdle()

        assertEquals(0, repository.syncs)
        assertNotNull(viewModel.currentState.errorMessage)
        viewModel.clear()
    }

    @Test
    fun reconnectThenSyncSucceedsForBrokenSource() = runTest {
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(SourcesIntent.Initialize)
        viewModel.onIntent(SourcesIntent.Reconnect("jira"))
        testScheduler.advanceUntilIdle()

        assertEquals(2, viewModel.currentState.activeIntegrations)

        viewModel.onIntent(SourcesIntent.Sync("jira"))
        testScheduler.advanceUntilIdle()
        assertEquals(1, repository.syncs)
        assertTrue(viewModel.currentState.syncingSourceIds.isEmpty())
        viewModel.clear()
    }
}
