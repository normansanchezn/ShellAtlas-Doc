package com.shelldocs.core.domain.usecase.assistant

import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.domain.entity.assistant.DocumentHealth
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.DocumentStatus
import kotlin.time.Duration.Companion.days

/**
 * Deterministic health audit used by both the assistant and the
 * Updates Pending triage. Penalizes staleness, bad status, thin content
 * and unfinished markers; never relies on an LLM so it is fully testable.
 */
class EvaluateDocumentHealthUseCase(private val timeProvider: TimeProvider) {

    operator fun invoke(document: Document): DocumentHealth {
        val issues = mutableListOf<String>()
        var score = 100

        val ageDays = ((timeProvider.now() - document.updatedAt) / 1.days).toInt()
        when {
            ageDays > STALE_HARD_DAYS -> {
                score -= 35
                issues += "Not updated in $ageDays days"
            }
            ageDays > STALE_SOFT_DAYS -> {
                score -= 15
                issues += "Approaching staleness ($ageDays days since last update)"
            }
        }

        when (document.status) {
            DocumentStatus.OUTDATED -> { score -= 30; issues += "Marked as outdated" }
            DocumentStatus.UPDATES_PENDING -> { score -= 20; issues += "Has pending review flags" }
            DocumentStatus.CONFLICTED -> { score -= 25; issues += "Conflicting versions detected" }
            DocumentStatus.DELETED_SOURCE -> { score -= 25; issues += "Original source was deleted" }
            else -> Unit
        }

        if (document.plainText.length < THIN_CONTENT_CHARS) {
            score -= 15
            issues += "Content is too thin to be actionable"
        }
        if (UNFINISHED_MARKERS.any { document.rawMarkdown.contains(it, ignoreCase = true) }) {
            score -= 10
            issues += "Contains unfinished TODO/TBD markers"
        }
        if (document.summary.isBlank()) {
            score -= 5
            issues += "Missing summary"
        }

        return DocumentHealth(
            documentId = document.id,
            score = score.coerceIn(0, 100),
            issues = issues,
        )
    }

    private companion object {
        const val STALE_SOFT_DAYS = 60
        const val STALE_HARD_DAYS = 120
        const val THIN_CONTENT_CHARS = 280
        val UNFINISHED_MARKERS = listOf("TODO", "TBD", "FIXME", "WIP")
    }
}
