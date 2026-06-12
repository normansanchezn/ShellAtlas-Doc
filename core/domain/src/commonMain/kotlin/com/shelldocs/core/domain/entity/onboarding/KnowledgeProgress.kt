package com.shelldocs.core.domain.entity.onboarding

/** Aggregate progress through the guided knowledge-transfer checkpoints. */
data class KnowledgeProgress(
    val completed: Int,
    val total: Int,
) {
    val percent: Int get() = if (total == 0) 0 else (completed * 100) / total
}
