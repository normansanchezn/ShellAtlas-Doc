package com.shelldocs.core.domain.entity.document

/** One confirmed metadata value ready to be applied to a document. */
data class MetadataAssignment(
    val attribute: MetadataAttribute,
    val value: String,
)
