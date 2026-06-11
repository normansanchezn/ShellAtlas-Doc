package com.shelldocs.core.domain.entity.dashboard

/** Percentage distribution of document statuses for the donut chart. */
data class StatusBreakdown(
    val publishedPercent: Int,
    val outdatedPercent: Int,
    val draftPercent: Int,
    val pendingPercent: Int,
)
