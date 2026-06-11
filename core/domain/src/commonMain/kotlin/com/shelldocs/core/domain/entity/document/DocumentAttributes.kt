package com.shelldocs.core.domain.entity.document

/** Flexible metadata stored in `document_attributes` (one row per key). */
data class DocumentAttributes(
    val owner: String = "",
    val module: String = "",
    val team: String = "",
    val platform: String = "",
    val parentFolderId: String? = null,
    val tags: List<String> = emptyList(),
)
