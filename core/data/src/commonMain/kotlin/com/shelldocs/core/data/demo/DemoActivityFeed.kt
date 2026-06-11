package com.shelldocs.core.data.demo

import com.shelldocs.core.domain.entity.dashboard.ActivityEvent
import com.shelldocs.core.domain.entity.dashboard.ActivityKind
import com.shelldocs.core.domain.entity.dashboard.AttentionItem
import com.shelldocs.core.domain.entity.dashboard.AttentionSeverity
import com.shelldocs.core.domain.entity.dashboard.UsagePoint
import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

/**
 * Dashboard signals that, in production, come from `audit_logs` and the
 * assistant telemetry; seeded here to mirror the design reference.
 */
object DemoActivityFeed {

    const val DOCS_DELTA_THIS_WEEK = 8
    const val OUTDATED_DELTA_THIS_WEEK = 3
    const val AI_QUERIES_THIS_WEEK = 371
    const val AI_QUERIES_DELTA_PERCENT = 24
    const val SOURCES_SYNCED = 2
    const val SOURCES_TOTAL = 3
    const val AI_ACCURACY_PERCENT = 94

    val weeklyUsage: List<UsagePoint> = listOf(
        UsagePoint("Mon", 31),
        UsagePoint("Tue", 58),
        UsagePoint("Wed", 52),
        UsagePoint("Thu", 96),
        UsagePoint("Fri", 64),
        UsagePoint("Sat", 22),
        UsagePoint("Sun", 18),
    )

    @OptIn(ExperimentalTime::class)
    val recentActivity: List<ActivityEvent> = listOf(
        ActivityEvent("act-1", "Elena Vargas", "EV", ActivityKind.PUBLISHED, "iOS Session Recovery", Instant.parse("2026-06-11T10:12:00Z")),
        ActivityEvent("act-2", "Marcus Chen", "MC", ActivityKind.MARKED_OUTDATED, "Loyalty Points v1", Instant.parse("2026-06-11T09:56:00Z")),
        ActivityEvent("act-3", "Priya Sharma", "PS", ActivityKind.EDITED, "Station Locator API", Instant.parse("2026-06-11T09:14:00Z")),
        ActivityEvent("act-4", "James O'Brien", "JO", ActivityKind.REVIEWED, "ExtB Architecture", Instant.parse("2026-06-11T08:14:00Z")),
        ActivityEvent("act-5", "AI Assistant", "AI", ActivityKind.FLAGGED_STALE, "Push Notification Flow", Instant.parse("2026-06-11T07:14:00Z")),
        ActivityEvent("act-6", "Sofia Reyes", "SR", ActivityKind.CREATED, "Android Auth Tokens", Instant.parse("2026-06-11T05:14:00Z")),
    )

    val attentionItems: List<AttentionItem> = listOf(
        AttentionItem(
            id = "attn-outdated",
            headline = "18 outdated documents",
            detail = "User Profile module most affected",
            severity = AttentionSeverity.WARNING,
        ),
        AttentionItem(
            id = "attn-jira",
            headline = "Jira sync failed 2h ago",
            detail = "Last successful sync: June 7",
            severity = AttentionSeverity.ERROR,
        ),
        AttentionItem(
            id = "attn-coverage",
            headline = "Coverage dropped 4%",
            detail = "Loyalty module needs new docs",
            severity = AttentionSeverity.WARNING,
        ),
    )
}
