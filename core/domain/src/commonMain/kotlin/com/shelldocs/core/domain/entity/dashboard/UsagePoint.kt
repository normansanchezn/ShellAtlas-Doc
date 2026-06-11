package com.shelldocs.core.domain.entity.dashboard

/** Assistant queries on a given day, for the usage bar chart. */
data class UsagePoint(
    val dayLabel: String,
    val queries: Int,
)
