@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.feature.updates.presentation

import com.shelldocs.core.common.error.ErrorDialogState
import com.shelldocs.core.common.mvi.MviState
import com.shelldocs.core.domain.entity.document.ContentBlock
import com.shelldocs.core.domain.entity.document.DocumentAttributes

/** Snapshot of the AI Suggested Update review workflow: preview, single editor, draft, metadata, confirm, sync. */
data class AiUpdateState(
    val analysisStage: AnalysisStage? = AnalysisStage.ANALYZING_DOCUMENT,
    val applyStage: ApplyStage? = null,
    val documentId: String = "",
    val documentTitle: String = "",
    val attributes: DocumentAttributes = DocumentAttributes(),
    val currentContentBlocks: List<ContentBlock> = emptyList(),
    val suggestedMarkdown: String = "",
    val hasSuggestedChanges: Boolean = false,
    val showMetadataDialog: Boolean = false,
    val metadataDraft: DocumentAttributes = DocumentAttributes(),
    val showConfirmDialog: Boolean = false,
    val errorDialog: ErrorDialogState? = null,
    val isAdmin: Boolean = false,
) : MviState {

    val isAnalyzing: Boolean get() = analysisStage != null
    val isApplying: Boolean get() = applyStage != null
    val ownerName: String get() = attributes.owner

    /** Save Changes is enabled only while the editor still has content. */
    val canSave: Boolean get() = hasSuggestedChanges && suggestedMarkdown.isNotBlank() && !isApplying
}
