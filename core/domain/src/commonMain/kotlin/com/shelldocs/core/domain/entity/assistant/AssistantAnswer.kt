package com.shelldocs.core.domain.entity.assistant

/** Grounded response produced by the assistant engine. */
data class AssistantAnswer(
    val markdown: String,
    val confidence: AnswerConfidence,
    val sources: List<AnswerSource>,
    val intent: AssistantIntentType,
    val fromCache: Boolean = false,
)
