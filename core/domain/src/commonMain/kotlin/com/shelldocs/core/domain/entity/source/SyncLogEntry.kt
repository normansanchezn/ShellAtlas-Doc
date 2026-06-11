package com.shelldocs.core.domain.entity.source

import kotlinx.datetime.Instant

/** Row of the Sync Activity Log (`sync_runs`). */
data class SyncLogEntry(
    val id: String,
    val sourceKind: SourceKind,
    val outcome: SyncOutcome,
    val message: String,
    val newDocs: Int,
    val updatedDocs: Int,
    val occurredAt: Instant,
)
