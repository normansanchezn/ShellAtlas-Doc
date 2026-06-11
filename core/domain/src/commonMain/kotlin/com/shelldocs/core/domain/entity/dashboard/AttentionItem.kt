package com.shelldocs.core.domain.entity.dashboard

/** "Needs Attention" banner at the bottom of the dashboard. */
data class AttentionItem(
    val id: String,
    val headline: String,
    val detail: String,
    val severity: AttentionSeverity,
)
