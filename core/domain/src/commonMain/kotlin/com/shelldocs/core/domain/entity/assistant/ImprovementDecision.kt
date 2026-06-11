package com.shelldocs.core.domain.entity.assistant

/**
 * The assistant's judgement on whether a document is worth improving.
 * When [shouldImprove] is false the assistant explicitly explains why the
 * document is fine as-is instead of rewriting it.
 */
data class ImprovementDecision(
    val documentId: String,
    val shouldImprove: Boolean,
    val healthScore: Int,
    val reasons: List<String>,
    val suggestions: List<String>,
)
