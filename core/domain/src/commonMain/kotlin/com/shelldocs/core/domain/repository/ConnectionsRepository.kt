package com.shelldocs.core.domain.repository

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.connection.ConnectionStatus

/** Real-time health of every system the app talks to: LLM, integrations, database. */
interface ConnectionsRepository {
    suspend fun statuses(): DomainResult<List<ConnectionStatus>>
}
