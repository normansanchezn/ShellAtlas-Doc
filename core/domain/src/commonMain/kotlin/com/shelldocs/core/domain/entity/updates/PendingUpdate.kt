package com.shelldocs.core.domain.entity.updates

import kotlinx.datetime.Instant

/** Row of the Updates Pending triage table. */
data class PendingUpdate(
    val documentId: String,
    val documentTitle: String,
    val module: String,
    val team: String,
    val risk: RiskLevel,
    val ageDays: Int,
    val impactScore: Int,
    val ownerName: String,
    val ownerInitials: String,
    val lastReview: Instant,
)
