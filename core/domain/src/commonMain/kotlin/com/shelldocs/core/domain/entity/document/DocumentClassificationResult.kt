package com.shelldocs.core.domain.entity.document

import kotlin.time.ExperimentalTime

/** Result of running automatic metadata classification against one document. */
data class DocumentClassificationResult @OptIn(ExperimentalTime::class) constructor(
    val documentId: String,
    val documentTitle: String,
    val status: MetadataClassificationStatus,
    val missingAttributes: List<MetadataAttribute>,
    val suggestions: List<MetadataSuggestion>,
    val sourceType: SourceType,
    val classifiedAt: kotlin.time.Instant,
    val developmentArea: DevelopmentArea? = null,
)
