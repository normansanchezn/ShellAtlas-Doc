package com.shelldocs.core.domain.entity.source

import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

/** Row of the Sync Activity Log (`sync_runs`). */
data class SyncLogEntry @OptIn(ExperimentalTime::class) constructor(
    val id: String,
    val sourceKind: SourceKind,
    val outcome: SyncOutcome,
    val message: String,
    val newDocs: Int,
    val updatedDocs: Int,
    val occurredAt: kotlin.time.Instant,
)
