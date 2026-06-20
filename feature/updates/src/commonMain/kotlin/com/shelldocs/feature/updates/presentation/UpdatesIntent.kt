package com.shelldocs.feature.updates.presentation

import com.shelldocs.core.common.mvi.MviIntent
import com.shelldocs.core.domain.entity.document.MetadataAttribute
import com.shelldocs.core.domain.entity.updates.RiskLevel

sealed interface UpdatesIntent : MviIntent {
    data object Initialize : UpdatesIntent
    data object ScanNow : UpdatesIntent
    data class ToggleRiskFilter(val risk: RiskLevel) : UpdatesIntent
    data object DismissError : UpdatesIntent
    data class SelectTab(val tab: DocumentationHealthTab) : UpdatesIntent
    data class AcceptMetadataSuggestion(val documentId: String, val attribute: MetadataAttribute) : UpdatesIntent
    data class AssignMetadata(val documentId: String, val attribute: MetadataAttribute, val value: String) : UpdatesIntent
    data class SetManualRisk(val documentId: String, val risk: RiskLevel?) : UpdatesIntent
    data class OpenUpdate(val documentId: String) : UpdatesIntent
}
