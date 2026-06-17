package com.shelldocs.core.domain.entity.assistant

/** Grounded response produced by the assistant engine. */
data class AssistantAnswer(
    val markdown: String,
    val confidence: AnswerConfidence,
    val sources: List<AnswerSource>,
    val intent: AssistantIntentType,
    val fromCache: Boolean = false,
    /** True when this is a placeholder telling the user the AI model is down, not a real grounded/LLM answer. */
    val isUnavailable: Boolean = false,
)
