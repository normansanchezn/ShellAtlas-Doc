package com.shelldocs.core.domain.entity.dashboard

import kotlinx.datetime.Instant

/** Recent Activity feed item (sourced from `audit_logs`). */
data class ActivityEvent(
    val id: String,
    val actorName: String,
    val actorInitials: String,
    val kind: ActivityKind,
    val target: String,
    val occurredAt: Instant,
)
