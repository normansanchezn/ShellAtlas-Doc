package com.shelldocs.core.domain.entity.document

import kotlinx.datetime.Instant

/** Acknowledgement returned after an autosaved draft upsert. */
data class DraftReceipt(
    val documentId: String,
    val contentHash: String,
    val savedAt: Instant,
)
