package com.shelldocs.core.domain.entity.document

import kotlin.time.ExperimentalTime

/** AI Suggested Update screen payload: read-only current doc + editable suggestion lines. */
data class DocumentUpdateSuggestion @OptIn(ExperimentalTime::class) constructor(
    val documentId: String,
    val documentTitle: String,
    val ownerName: String,
    val currentMarkdown: String,
    val lines: List<SuggestionLine>,
    val generatedAt: kotlin.time.Instant,
) {
    val hasAiContent: Boolean = lines.any { it.origin != LineOrigin.ORIGINAL }
}
