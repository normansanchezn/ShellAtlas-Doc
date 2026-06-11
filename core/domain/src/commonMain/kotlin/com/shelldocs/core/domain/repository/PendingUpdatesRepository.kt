package com.shelldocs.core.domain.repository

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.updates.PendingUpdate

/** Maintenance triage feed for the Updates Pending screen. */
interface PendingUpdatesRepository {

    suspend fun pendingUpdates(): DomainResult<List<PendingUpdate>>

    suspend fun scanNow(): DomainResult<List<PendingUpdate>>
}
