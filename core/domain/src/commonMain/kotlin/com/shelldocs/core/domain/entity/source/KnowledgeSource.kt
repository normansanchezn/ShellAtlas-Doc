package com.shelldocs.core.domain.entity.source

import kotlinx.datetime.Instant

/** External integration that feeds documents into the knowledge base. */
data class KnowledgeSource(
    val id: String,
    val kind: SourceKind,
    val host: String,
    val status: SourceStatus,
    val importedDocs: Int,
    val lastSyncAt: Instant?,
    val errorMessage: String? = null,
)
