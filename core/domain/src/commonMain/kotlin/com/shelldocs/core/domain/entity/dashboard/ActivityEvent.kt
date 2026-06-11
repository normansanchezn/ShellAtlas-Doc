package com.shelldocs.core.domain.entity.dashboard

import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

/** Recent Activity feed item (sourced from `audit_logs`). */
data class ActivityEvent @OptIn(ExperimentalTime::class) constructor(
    val id: String,
    val actorName: String,
    val actorInitials: String,
    val kind: ActivityKind,
    val target: String,
    val occurredAt: kotlin.time.Instant,
)
