package com.shelldocs.core.domain.entity.assistant

/** Whether a local/remote LLM backs the assistant or the grounded fallback runs. */
data class AssistantAvailability(
    val isLlmReachable: Boolean,
    val modelName: String?,
    val statusMessage: String,
)
