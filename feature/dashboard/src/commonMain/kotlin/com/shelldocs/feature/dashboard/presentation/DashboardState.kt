package com.shelldocs.feature.dashboard.presentation

import com.shelldocs.core.common.error.ErrorDialogState
import com.shelldocs.core.common.mvi.MviState
import com.shelldocs.core.domain.entity.dashboard.DashboardMetrics

/** Snapshot of the knowledge-operations dashboard. */
data class DashboardState(
    val isLoading: Boolean = false,
    val metrics: DashboardMetrics? = null,
    val errorDialog: ErrorDialogState? = null,
) : MviState
