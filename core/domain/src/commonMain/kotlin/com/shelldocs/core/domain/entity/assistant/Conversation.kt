package com.shelldocs.core.domain.entity.assistant

import kotlinx.datetime.Instant

/** Stored assistant thread, listed in the conversations panel. */
data class Conversation(
    val id: String,
    val title: String,
    val messages: List<AssistantMessage>,
    val updatedAt: Instant,
)
