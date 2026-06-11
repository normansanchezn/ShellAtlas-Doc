package com.shelldocs.feature.updates.presentation

import com.shelldocs.core.common.mvi.MviState
import com.shelldocs.core.domain.entity.updates.PendingUpdate
import com.shelldocs.core.domain.entity.updates.RiskLevel

/** Snapshot of the Updates Pending triage screen. */
data class UpdatesState(
    val isLoading: Boolean = false,
    val isScanning: Boolean = false,
    val updates: List<PendingUpdate> = emptyList(),
    val riskFilter: RiskLevel? = null,
    val errorMessage: String? = null,
) : MviState {

    val filteredUpdates: List<PendingUpdate> =
        riskFilter?.let { filter -> updates.filter { it.risk == filter } } ?: updates

    val countsByRisk: Map<RiskLevel, Int> =
        RiskLevel.entries.associateWith { risk -> updates.count { it.risk == risk } }
}
