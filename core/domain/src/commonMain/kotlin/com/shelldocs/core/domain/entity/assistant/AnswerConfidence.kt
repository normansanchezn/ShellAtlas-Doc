package com.shelldocs.core.domain.entity.assistant

/** Confidence the assistant reports next to each grounded answer. */
enum class AnswerConfidence(val percentage: Int) {
    HIGH(94),
    MEDIUM(76),
    LOW(48),
    NOT_ENOUGH_INFORMATION(0);

    companion object {
        fun fromRetrievalScore(score: Double): AnswerConfidence = when {
            score >= 0.62 -> HIGH
            score >= 0.32 -> MEDIUM
            score > 0.0 -> LOW
            else -> NOT_ENOUGH_INFORMATION
        }
    }
}
