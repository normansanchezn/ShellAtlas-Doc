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

        /**
         * Documentation Health auto-classification: Critical when stale beyond a
         * year, the document's recorded version trails the latest app version, or
         * any upstream system (ADO, Program Board, Confluence, Release Notes) has
         * unreflected changes. Otherwise Low. Medium is never auto-assigned — it
         * is admin-only (see [com.shelldocs.core.domain.usecase.updates.SetManualRiskLevelUseCase]).
         */
        fun fromSignals(reviewAgeDays: Int, versionMismatch: Boolean, hasUnreflectedUpstreamChanges: Boolean): RiskLevel =
            if (reviewAgeDays > 365 || versionMismatch || hasUnreflectedUpstreamChanges) CRITICAL else LOW

        /** Documentation Health only ever auto/manually assigns these three — [HIGH] is legacy/unused there. */
        val DOCUMENTATION_HEALTH_LEVELS = listOf(CRITICAL, MEDIUM, LOW)
    }
}
