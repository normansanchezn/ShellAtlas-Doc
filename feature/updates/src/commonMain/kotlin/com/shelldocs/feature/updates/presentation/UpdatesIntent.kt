package com.shelldocs.feature.updates.presentation

import com.shelldocs.core.common.mvi.MviIntent
import com.shelldocs.core.domain.entity.updates.RiskLevel

sealed interface UpdatesIntent : MviIntent {
    data object Initialize : UpdatesIntent
    data object ScanNow : UpdatesIntent
    data class ToggleRiskFilter(val risk: RiskLevel) : UpdatesIntent
}
