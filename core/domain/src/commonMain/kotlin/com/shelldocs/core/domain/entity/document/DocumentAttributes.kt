package com.shelldocs.core.domain.entity.document

import kotlin.time.ExperimentalTime

/** Flexible metadata stored in `document_attributes` (one row per key). */
data class DocumentAttributes @OptIn(ExperimentalTime::class) constructor(
    val owner: String = "",
    val module: String = "",
    val team: String = "",
    val platform: String = "",
    val parentFolderId: String? = null,
    val tags: List<String> = emptyList(),
    val area: Area? = null,
    val applicationVersion: String = "",
    val lastReviewedDate: kotlin.time.Instant? = null,
    val sourceType: SourceType = SourceType.OTHER,
)
