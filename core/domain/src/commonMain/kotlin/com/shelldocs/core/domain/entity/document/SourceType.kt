package com.shelldocs.core.domain.entity.document

/** External system a document's content or metadata originates from. */
enum class SourceType(val displayName: String) {
    CONFLUENCE("Confluence"),
    AZURE_DEVOPS("Azure DevOps"),
    WIKI("Wiki"),
    OTHER("Other");

    companion object {
        fun fromKey(key: String?): SourceType =
            entries.firstOrNull { it.name.equals(key, ignoreCase = true) || it.displayName.equals(key, ignoreCase = true) } ?: OTHER
    }
}
