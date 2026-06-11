package com.shelldocs.core.domain.entity.document

/** Lifecycle of a knowledge document; mirrors the `documents.status` CHECK. */
enum class DocumentStatus(val key: String, val displayName: String) {
    DRAFT(key = "draft", displayName = "Draft"),
    PUBLISHED(key = "published", displayName = "Published"),
    UPDATES_PENDING(key = "updates_pending", displayName = "Needs Review"),
    OUTDATED(key = "outdated", displayName = "Outdated"),
    CONFLICTED(key = "conflicted", displayName = "Conflicted"),
    ARCHIVED(key = "archived", displayName = "Archived"),
    LOCKED(key = "locked", displayName = "Locked"),
    DELETED_SOURCE(key = "deleted_source", displayName = "Source Deleted");

    companion object {
        fun fromKey(key: String?): DocumentStatus =
            entries.firstOrNull { it.key == key?.lowercase() } ?: DRAFT
    }
}
