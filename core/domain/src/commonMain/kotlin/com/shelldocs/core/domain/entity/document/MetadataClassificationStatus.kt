package com.shelldocs.core.domain.entity.document

/** Outcome of automatic metadata classification for a document. */
enum class MetadataClassificationStatus(val displayName: String) {
    READY("Ready"),
    NEEDS_REVIEW("Needs Review"),
    REQUIRES_ATTENTION("Requires Attention");

    companion object {
        /** [MetadataAttribute.REQUIRED] missing -> attention; any missing/low-confidence -> review. */
        fun fromMissingAttributes(missingRequired: List<MetadataAttribute>, missingOptional: List<MetadataAttribute>): MetadataClassificationStatus =
            when {
                missingRequired.isNotEmpty() -> REQUIRES_ATTENTION
                missingOptional.isNotEmpty() -> NEEDS_REVIEW
                else -> READY
            }
    }
}
