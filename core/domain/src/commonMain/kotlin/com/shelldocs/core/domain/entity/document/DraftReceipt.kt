package com.shelldocs.core.domain.entity.document

import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

/** Acknowledgement returned after an autosaved draft upsert. */
data class DraftReceipt @OptIn(ExperimentalTime::class) constructor(
    val documentId: String,
    val contentHash: String,
    val savedAt: kotlin.time.Instant,
)
