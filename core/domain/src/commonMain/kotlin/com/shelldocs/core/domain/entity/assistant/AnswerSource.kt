package com.shelldocs.core.domain.entity.assistant

/** Document citation attached to an assistant answer. */
data class AnswerSource(
    val documentId: String,
    val title: String,
    val breadcrumb: String,
    val relevance: Int,
)
