package com.shelldocs.core.domain.entity.document

/** AI-proposed value for a missing or low-confidence metadata attribute. */
data class MetadataSuggestion(
    val attribute: MetadataAttribute,
    val suggestedValue: String,
    val confidencePercent: Int,
)
