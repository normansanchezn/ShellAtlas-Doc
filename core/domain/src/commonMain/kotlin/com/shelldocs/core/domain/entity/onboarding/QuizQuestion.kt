package com.shelldocs.core.domain.entity.onboarding

/** One multiple-choice question gating advancement past a [KnowledgeCheckpoint]. */
data class QuizQuestion(
    val id: String,
    val checkpointId: String,
    val prompt: String,
    val options: List<String>,
    val correctOptionIndex: Int,
)
