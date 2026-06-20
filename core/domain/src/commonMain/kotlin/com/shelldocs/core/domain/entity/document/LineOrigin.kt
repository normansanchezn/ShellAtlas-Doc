package com.shelldocs.core.domain.entity.document

/** Provenance of a line in an [AI Suggested Update][DocumentUpdateSuggestion]. */
enum class LineOrigin {
    /** Untouched line from the current published document. Never highlighted. */
    ORIGINAL,

    /** AI-generated or AI-modified line, pending human review. Highlighted. */
    AI_SUGGESTED,

    /** Was [AI_SUGGESTED] but a human edited it — highlight removed, counts as reviewed. */
    HUMAN_REVIEWED,
}
