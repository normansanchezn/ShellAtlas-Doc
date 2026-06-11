package com.shelldocs.core.domain.entity.source

import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

/** External integration that feeds documents into the knowledge base. */
data class KnowledgeSource @OptIn(ExperimentalTime::class) constructor(
    val id: String,
    val kind: SourceKind,
    val host: String,
    val status: SourceStatus,
    val importedDocs: Int,
    val lastSyncAt: kotlin.time.Instant?,
    val errorMessage: String? = null,
)
