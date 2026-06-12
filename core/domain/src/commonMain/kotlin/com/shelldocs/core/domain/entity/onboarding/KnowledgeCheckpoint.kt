package com.shelldocs.core.domain.entity.onboarding

/** A single guided knowledge-transfer step the assistant can walk a user through. */
data class KnowledgeCheckpoint(
    val id: String,
    val order: Int,
    val title: String,
    val instruction: String,
    val documentId: String?,
)
