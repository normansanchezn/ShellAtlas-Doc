package com.shelldocs.feature.updates.presentation

import com.shelldocs.core.common.mvi.MviEffect

sealed interface UpdatesEffect : MviEffect {
    data class OpenDocument(val documentId: String) : UpdatesEffect
    data class MetadataUpdated(val documentId: String) : UpdatesEffect
}
