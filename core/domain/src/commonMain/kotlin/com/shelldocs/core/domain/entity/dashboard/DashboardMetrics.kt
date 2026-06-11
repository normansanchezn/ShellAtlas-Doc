package com.shelldocs.core.domain.entity.dashboard

/** Aggregated knowledge-operations overview. */
data class DashboardMetrics(
    val totalDocuments: Int,
    val totalDocumentsDelta: Int,
    val outdatedDocuments: Int,
    val outdatedDocumentsDelta: Int,
    val coverageScorePercent: Int,
    val aiQueriesThisWeek: Int,
    val aiQueriesDeltaPercent: Int,
    val knowledgeHealthScore: Int,
    val docsReviewedPercent: Int,
    val sourcesSynced: Int,
    val sourcesTotal: Int,
    val aiAccuracyPercent: Int,
    val staleRatePercent: Int,
    val moduleCoverage: List<ModuleCoverage>,
    val statusBreakdown: StatusBreakdown,
    val usage: List<UsagePoint>,
    val recentActivity: List<ActivityEvent>,
    val attentionItems: List<AttentionItem>,
)
