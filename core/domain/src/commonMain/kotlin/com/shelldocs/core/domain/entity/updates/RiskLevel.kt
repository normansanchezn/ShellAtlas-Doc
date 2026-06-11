package com.shelldocs.core.domain.entity.updates

/** Triage severity for documents pending maintenance. */
enum class RiskLevel(val displayName: String) {
    CRITICAL("Critical"),
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low");

    companion object {
        /** Derives risk from a 0-100 impact score (age + signals weighted). */
        fun fromImpactScore(score: Int): RiskLevel = when {
            score >= 80 -> CRITICAL
            score >= 60 -> HIGH
            score >= 40 -> MEDIUM
            else -> LOW
        }
    }
}
