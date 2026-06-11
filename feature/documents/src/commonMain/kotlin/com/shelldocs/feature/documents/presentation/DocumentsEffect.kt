package com.shelldocs.feature.documents.presentation

import com.shelldocs.core.common.mvi.MviEffect

sealed interface DocumentsEffect : MviEffect {
    data class ShowNotice(val message: String) : DocumentsEffect
}
