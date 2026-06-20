package com.shelldocs.core.domain.entity.document

/** One editable line of the AI Suggested Update panel. */
data class SuggestionLine(
    val text: String,
    val origin: LineOrigin,
)
