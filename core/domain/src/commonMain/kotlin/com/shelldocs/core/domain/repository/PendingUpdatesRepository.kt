package com.shelldocs.core.domain.repository

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.updates.PendingUpdate
import com.shelldocs.core.domain.entity.updates.RiskLevel

/** Maintenance triage feed for the Documentation Health screen. */
interface PendingUpdatesRepository {

    suspend fun pendingUpdates(): DomainResult<List<PendingUpdate>>

    suspend fun scanNow(): DomainResult<List<PendingUpdate>>

    /** Admin-only manual override; pass `null` to clear back to the auto-computed risk. */
    suspend fun setManualRisk(documentId: String, risk: RiskLevel?): DomainResult<PendingUpdate>
}
