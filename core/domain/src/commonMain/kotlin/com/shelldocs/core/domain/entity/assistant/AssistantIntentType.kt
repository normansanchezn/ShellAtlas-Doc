package com.shelldocs.core.domain.entity.assistant

/**
 * What the user is actually asking for. Detected before answering so the
 * assistant can explain flows, audit document health or answer questions
 * with the right strategy.
 */
enum class AssistantIntentType {
    QUESTION,
    EXPLAIN_FLOW,
    IMPROVE_DOCUMENT,
    SUMMARIZE,
    CREATE_DOCUMENT,
}
