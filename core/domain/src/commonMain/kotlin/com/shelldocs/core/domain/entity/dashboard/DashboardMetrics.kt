package com.shelldocs.core.domain.entity.dashboard

/** Aggregated knowledge-operations overview, computed entirely from existing document/checkpoint data. */
data class DashboardMetrics(
    val knowledgeTransferCompleted: Int,
    val knowledgeTransferTotal: Int,
    val knowledgeTransferPercent: Int,
    val healthyDocuments: Int,
    val attentionDocuments: Int,
    val areaCoverage: List<AreaCoverage>,
    val statusBreakdown: List<StatusCount>,
    val aiUsageCount: Int,
    val topOwners: List<OwnerStat>,
)
