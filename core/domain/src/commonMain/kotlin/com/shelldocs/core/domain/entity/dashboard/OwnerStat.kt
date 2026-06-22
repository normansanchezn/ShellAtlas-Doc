package com.shelldocs.core.domain.entity.dashboard

/** One row of the Top Owners leaderboard. */
data class OwnerStat(
    val owner: String,
    val documentCount: Int,
)
