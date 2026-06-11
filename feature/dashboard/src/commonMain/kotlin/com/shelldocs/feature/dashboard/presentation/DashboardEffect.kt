package com.shelldocs.feature.dashboard.presentation

import com.shelldocs.core.common.mvi.MviEffect

sealed interface DashboardEffect : MviEffect {
    data class OpenSection(val route: String) : DashboardEffect
}
