package com.shelldocs.feature.dashboard.presentation

import com.shelldocs.core.common.mvi.MviIntent

sealed interface DashboardIntent : MviIntent {
    data object Initialize : DashboardIntent
    data object Refresh : DashboardIntent
    data object DismissError : DashboardIntent
}
