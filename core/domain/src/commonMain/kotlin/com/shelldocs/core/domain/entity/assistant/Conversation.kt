package com.shelldocs.core.domain.entity.assistant

import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

/** Stored assistant thread, listed in the conversations panel. */
data class Conversation @OptIn(ExperimentalTime::class) constructor(
    val id: String,
    val title: String,
    val messages: List<AssistantMessage>,
    val updatedAt: kotlin.time.Instant,
)
