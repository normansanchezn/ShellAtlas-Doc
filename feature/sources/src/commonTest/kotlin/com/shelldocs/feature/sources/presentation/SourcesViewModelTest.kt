package com.shelldocs.feature.sources.presentation

import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.connection.ConnectionState
import com.shelldocs.core.domain.entity.connection.ConnectionStatus
import com.shelldocs.core.domain.repository.ConnectionsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private class SingleDispatcher(dispatcher: CoroutineDispatcher) : DispatcherProvider {
    override val main = dispatcher
    override val default = dispatcher
    override val io = dispatcher
}

private class FakeConnectionsRepository : ConnectionsRepository {
    var shouldFail = false
    var statusesList = listOf(
        ConnectionStatus("ollama", "Ollama", ConnectionState.CONNECTED, "llama3.2"),
        ConnectionStatus("confluence", "Confluence", ConnectionState.CONNECTED, "shell-engineering.atlassian.net"),
        ConnectionStatus("jira", "Jira", ConnectionState.DISABLED),
        ConnectionStatus("azure-devops", "Azure DevOps", ConnectionState.DISABLED),
        ConnectionStatus("database", "Database", ConnectionState.CONNECTED),
    )

    override suspend fun statuses(): DomainResult<List<ConnectionStatus>> =
        if (shouldFail) DomainResult.failure(AppError.Network("offline")) else DomainResult.success(statusesList)
}

class SourcesViewModelTest {

    private val repository = FakeConnectionsRepository()

    private fun viewModel(scheduler: kotlinx.coroutines.test.TestCoroutineScheduler) = SourcesViewModel(
        connectionsRepository = repository,
        dispatchers = SingleDispatcher(StandardTestDispatcher(scheduler)),
    )

    @Test
    fun initializeLoadsRealConnectionStatuses() = runTest {
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(SourcesIntent.Initialize)
        testScheduler.advanceUntilIdle()

        assertEquals(5, viewModel.currentState.connections.size)
        assertEquals(
            ConnectionState.CONNECTED,
            viewModel.currentState.connections.first { it.id == "confluence" }.state
        )
        assertEquals(ConnectionState.DISABLED, viewModel.currentState.connections.first { it.id == "jira" }.state)
        viewModel.clear()
    }

    @Test
    fun initializeFailureShowsErrorDialog() = runTest {
        repository.shouldFail = true
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(SourcesIntent.Initialize)
        testScheduler.advanceUntilIdle()

        assertNotNull(viewModel.currentState.errorDialog)
        viewModel.clear()
    }

    @Test
    fun dismissErrorClearsDialog() = runTest {
        repository.shouldFail = true
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(SourcesIntent.Initialize)
        testScheduler.advanceUntilIdle()
        assertNotNull(viewModel.currentState.errorDialog)

        viewModel.onIntent(SourcesIntent.DismissError)
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.currentState.errorDialog)
        viewModel.clear()
    }

    @Test
    fun contactSupportIsNoOp() = runTest {
        val viewModel = viewModel(testScheduler)
        viewModel.onIntent(SourcesIntent.Initialize)
        testScheduler.advanceUntilIdle()

        viewModel.onIntent(SourcesIntent.ContactSupport)
        testScheduler.advanceUntilIdle()

        assertEquals(5, viewModel.currentState.connections.size)
        viewModel.clear()
    }
}
