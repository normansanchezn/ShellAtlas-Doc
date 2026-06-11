package com.shelldocs.core.domain.entity.document

import kotlinx.datetime.Instant

/** Aggregate root of the knowledge base. */
data class Document(
    val id: String,
    val title: String,
    val summary: String,
    val status: DocumentStatus,
    val classification: DocumentClassification,
    val rawMarkdown: String,
    val content: DocumentContent,
    val plainText: String,
    val attributes: DocumentAttributes,
    val createdAt: Instant,
    val updatedAt: Instant,
)
