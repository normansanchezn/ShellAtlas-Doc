package com.shelldocs.feature.updates.presentation

import com.shelldocs.core.common.error.ErrorDialogState
import com.shelldocs.core.common.mvi.MviState
import com.shelldocs.core.domain.entity.document.DocumentClassificationResult
import com.shelldocs.core.domain.entity.document.MetadataClassificationStatus
import com.shelldocs.core.domain.entity.updates.PendingUpdate
import com.shelldocs.core.domain.entity.updates.RiskLevel

/** Snapshot of the Documentation Health screen (Health table + Metadata Issues). */
data class UpdatesState(
    val isLoading: Boolean = false,
    val isScanning: Boolean = false,
    val updates: List<PendingUpdate> = emptyList(),
    val riskFilter: RiskLevel? = null,
    val errorDialog: ErrorDialogState? = null,
    val selectedTab: DocumentationHealthTab = DocumentationHealthTab.HEALTH,
    val isLoadingMetadataIssues: Boolean = false,
    val metadataIssues: List<DocumentClassificationResult> = emptyList(),
    val isAdmin: Boolean = false,
    val canUpdateDocuments: Boolean = false,
    val isLoadingHealthyDocuments: Boolean = false,
    val healthyDocuments: List<PendingUpdate> = emptyList(),
) : MviState {

    val filteredUpdates: List<PendingUpdate> =
        riskFilter?.let { filter -> updates.filter { it.risk == filter } } ?: updates

    val countsByRisk: Map<RiskLevel, Int> =
        RiskLevel.DOCUMENTATION_HEALTH_LEVELS.associateWith { risk -> updates.count { it.risk == risk } }

    val metadataIssuesRequiringAttention: Int =
        metadataIssues.count { it.status == MetadataClassificationStatus.REQUIRES_ATTENTION }
}
