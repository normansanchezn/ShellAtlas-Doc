package com.shelldocs.core.data.repository

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.data.assistant.OllamaClient
import com.shelldocs.core.data.network.ShellDocsApi
import com.shelldocs.core.data.network.dto.ConnectionFieldDto
import com.shelldocs.core.data.supabase.SupabasePostgrestApi
import com.shelldocs.core.domain.entity.connection.ConnectionState
import com.shelldocs.core.domain.entity.connection.ConnectionStatus
import com.shelldocs.core.domain.repository.ConnectionsRepository

/**
 * Real status for every system the app depends on — no seeded/demo data.
 * Each dependency is nullable because the app may run without it configured
 * (demo mode, Ollama disabled, etc.); absence reads as DISABLED/DISCONNECTED
 * rather than throwing.
 */
class RealConnectionsRepository(
    private val ollamaClient: OllamaClient?,
    private val api: ShellDocsApi?,
    private val postgrest: SupabasePostgrestApi?,
) : ConnectionsRepository {

    override suspend fun statuses(): DomainResult<List<ConnectionStatus>> {
        val ollama = ConnectionStatus(
            id = "ollama",
            name = "Ollama",
            state = when {
                ollamaClient == null -> ConnectionState.DISABLED
                ollamaClient.isReachable() -> ConnectionState.CONNECTED
                else -> ConnectionState.DISCONNECTED
            },
            detail = ollamaClient?.modelName,
        )

        val backend = api?.let { runCatching { it.connectionsStatus() }.getOrNull() }

        val confluence = backend?.confluence?.toStatus("confluence", "Confluence")
            ?: ConnectionStatus("confluence", "Confluence", ConnectionState.DISCONNECTED, "Backend not reachable")

        val jira = backend?.jira?.toStatus("jira", "Jira")
            ?: ConnectionStatus("jira", "Jira", ConnectionState.DISABLED)

        val azureDevops = backend?.azureDevops?.toStatus("azure-devops", "Azure DevOps")
            ?: ConnectionStatus("azure-devops", "Azure DevOps", ConnectionState.DISABLED)

        val database = backend?.database?.toStatus("database", "Database")
            ?: fallbackDatabaseStatus()

        return DomainResult.success(listOf(ollama, confluence, jira, azureDevops, database))
    }

    private suspend fun fallbackDatabaseStatus(): ConnectionStatus {
        val postgrestClient = postgrest ?: return ConnectionStatus("database", "Database", ConnectionState.DISCONNECTED)
        val result = postgrestClient.testConnection()
        return ConnectionStatus(
            id = "database",
            name = "Database",
            state = if (result.isSuccess) ConnectionState.CONNECTED else ConnectionState.ERROR,
            detail = result.exceptionOrNull()?.message,
        )
    }

    private fun ConnectionFieldDto.toStatus(id: String, name: String): ConnectionStatus = ConnectionStatus(
        id = id,
        name = name,
        state = when (status) {
            "connected" -> ConnectionState.CONNECTED
            "disabled" -> ConnectionState.DISABLED
            "error" -> ConnectionState.ERROR
            else -> ConnectionState.DISCONNECTED
        },
        detail = host ?: detail,
    )
}
