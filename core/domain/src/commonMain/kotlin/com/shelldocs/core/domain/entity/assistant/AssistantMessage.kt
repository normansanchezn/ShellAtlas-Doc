package com.shelldocs.core.domain.entity.assistant

import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

/** One bubble of the assistant conversation. */
data class AssistantMessage @OptIn(ExperimentalTime::class) constructor(
    val id: String,
    val role: MessageRole,
    val markdown: String,
    val confidence: AnswerConfidence? = null,
    val sources: List<AnswerSource> = emptyList(),
    val createdAt: kotlin.time.Instant,
)
