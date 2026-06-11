package com.shelldocs.core.domain.entity.assistant

/** Outcome of auditing a single document (0-100, higher is healthier). */
data class DocumentHealth(
    val documentId: String,
    val score: Int,
    val issues: List<String>,
) {
    val isHealthy: Boolean get() = score >= HEALTHY_THRESHOLD

    companion object {
        const val HEALTHY_THRESHOLD = 70
    }
}
