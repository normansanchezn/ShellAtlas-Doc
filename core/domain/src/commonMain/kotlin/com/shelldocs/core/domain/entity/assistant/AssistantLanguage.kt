package com.shelldocs.core.domain.entity.assistant

/** Language the assistant should reply in, detected from the user's message. */
enum class AssistantLanguage {
    ENGLISH,
    SPANISH,
    FRENCH,
    ;

    /** Human-readable name to embed in LLM prompts. */
    val promptName: String
        get() = when (this) {
            ENGLISH -> "English"
            SPANISH -> "Spanish"
            FRENCH -> "French"
        }
}
