package com.shelldocs.core.domain.entity.document

/** Structured representation of a document body (`content_json`). */
data class DocumentContent(
    val schemaVersion: Int = 1,
    val blocks: List<ContentBlock> = emptyList(),
)
