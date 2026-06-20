package com.shelldocs.core.domain.entity.updates

import com.shelldocs.core.domain.entity.document.Area
import kotlin.time.ExperimentalTime

/** Row of the Documentation Health triage table. */
data class PendingUpdate @OptIn(ExperimentalTime::class) constructor(
    val documentId: String,
    val documentTitle: String,
    val module: String,
    val team: String,
    val risk: RiskLevel,
    val ageDays: Int,
    val impactScore: Int,
    val ownerName: String,
    val ownerInitials: String,
    val lastReview: kotlin.time.Instant,
    val area: Area? = null,
    val applicationVersion: String = "",
    val documentVersion: String = "",
    val manualRiskOverride: RiskLevel? = null,
)
