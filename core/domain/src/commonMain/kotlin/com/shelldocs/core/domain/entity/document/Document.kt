package com.shelldocs.core.domain.entity.document

import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

/** Aggregate root of the knowledge base. */
data class Document @OptIn(ExperimentalTime::class) constructor(
    val id: String,
    val title: String,
    val summary: String,
    val status: DocumentStatus,
    val classification: DocumentClassification,
    val rawMarkdown: String,
    val content: DocumentContent,
    val plainText: String,
    val attributes: DocumentAttributes,
    val createdAt: kotlin.time.Instant,
    val updatedAt: kotlin.time.Instant,
)
