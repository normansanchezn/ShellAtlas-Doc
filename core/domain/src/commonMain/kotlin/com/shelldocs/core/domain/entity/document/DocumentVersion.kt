package com.shelldocs.core.domain.entity.document

import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

/** Immutable snapshot stored in `document_versions`. */
data class DocumentVersion @OptIn(ExperimentalTime::class) constructor(
    val id: String,
    val documentId: String,
    val versionNumber: Int,
    val title: String,
    val rawMarkdown: String,
    val changeSummary: String,
    val createdAt: kotlin.time.Instant,
)
