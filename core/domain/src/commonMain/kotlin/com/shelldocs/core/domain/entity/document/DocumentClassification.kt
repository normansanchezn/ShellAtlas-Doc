package com.shelldocs.core.domain.entity.document

/** Data sensitivity tiers; mirrors `documents.classification`. */
enum class DocumentClassification(val key: String) {
    PUBLIC("public"),
    INTERNAL("internal"),
    CONFIDENTIAL("confidential"),
    RESTRICTED("restricted");

    companion object {
        fun fromKey(key: String?): DocumentClassification =
            entries.firstOrNull { it.key == key?.lowercase() } ?: INTERNAL
    }
}
