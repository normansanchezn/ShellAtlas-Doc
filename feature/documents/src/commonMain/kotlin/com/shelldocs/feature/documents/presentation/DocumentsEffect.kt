package com.shelldocs.feature.documents.presentation

import com.shelldocs.core.common.mvi.MviEffect

sealed interface DocumentsEffect : MviEffect {
    data class ShowNotice(val message: String) : DocumentsEffect
    data class ExportDocumentAsPdf(val documentId: String, val title: String, val markdown: String) : DocumentsEffect
}
