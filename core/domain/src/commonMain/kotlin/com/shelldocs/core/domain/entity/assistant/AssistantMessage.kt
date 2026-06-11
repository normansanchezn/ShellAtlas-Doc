package com.shelldocs.core.domain.entity.assistant

import kotlinx.datetime.Instant

/** One bubble of the assistant conversation. */
data class AssistantMessage(
    val id: String,
    val role: MessageRole,
    val markdown: String,
    val confidence: AnswerConfidence? = null,
    val sources: List<AnswerSource> = emptyList(),
    val createdAt: Instant,
)
