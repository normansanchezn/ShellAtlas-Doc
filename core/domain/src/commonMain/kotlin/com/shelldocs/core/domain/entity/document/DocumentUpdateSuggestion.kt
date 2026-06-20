package com.shelldocs.core.domain.entity.document

import kotlin.time.ExperimentalTime

/**
 * AI Suggested Update screen payload: a read-only preview of the current
 * document next to one continuous editable markdown suggestion — never
 * split into per-line fields.
 */
data class DocumentUpdateSuggestion @OptIn(ExperimentalTime::class) constructor(
    val documentId: String,
    val documentTitle: String,
    val attributes: DocumentAttributes,
    val currentContentBlocks: List<ContentBlock>,
    val currentMarkdown: String,
    val suggestedMarkdown: String,
    val generatedAt: kotlin.time.Instant,
) {
    val hasSuggestedChanges: Boolean = suggestedMarkdown != currentMarkdown
}
