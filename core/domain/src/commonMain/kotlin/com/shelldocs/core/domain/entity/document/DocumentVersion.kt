package com.shelldocs.core.domain.entity.document

import kotlinx.datetime.Instant

/** Immutable snapshot stored in `document_versions`. */
data class DocumentVersion(
    val id: String,
    val documentId: String,
    val versionNumber: Int,
    val title: String,
    val rawMarkdown: String,
    val changeSummary: String,
    val createdAt: Instant,
)
