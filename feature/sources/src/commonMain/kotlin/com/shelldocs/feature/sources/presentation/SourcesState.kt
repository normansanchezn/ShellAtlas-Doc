package com.shelldocs.feature.sources.presentation

import com.shelldocs.core.common.error.ErrorDialogState
import com.shelldocs.core.common.mvi.MviState
import com.shelldocs.core.domain.entity.connection.ConnectionStatus

/** Snapshot of the Connections screen. */
data class SourcesState(
    val isLoading: Boolean = false,
    val connections: List<ConnectionStatus> = emptyList(),
    val loadingMessage: String? = null,
    val errorDialog: ErrorDialogState? = null,
) : MviState {
    val isBusy: Boolean = isLoading || loadingMessage != null
}
