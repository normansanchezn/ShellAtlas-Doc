package com.shelldocs.core.domain.usecase.assistant

import com.shelldocs.core.domain.entity.assistant.DocumentHealth
import com.shelldocs.core.domain.entity.assistant.ImprovementDecision
import com.shelldocs.core.domain.entity.document.Document

/**
 * Decides whether a document deserves an improvement pass. A healthy
 * document gets an explicit "leave it alone" verdict with the rationale —
 * the assistant must know when NOT to touch content.
 */
class ShouldImproveDocumentUseCase(
    private val evaluateDocumentHealth: EvaluateDocumentHealthUseCase,
) {

    operator fun invoke(document: Document): ImprovementDecision {
        val health = evaluateDocumentHealth(document)
        return if (health.isHealthy) {
            ImprovementDecision(
                documentId = document.id,
                shouldImprove = false,
                healthScore = health.score,
                reasons = listOf(
                    "Health score ${health.score}/100 is above the ${DocumentHealth.HEALTHY_THRESHOLD} threshold",
                    "Rewriting healthy content risks introducing drift without adding value",
                ),
                suggestions = emptyList(),
            )
        } else {
            ImprovementDecision(
                documentId = document.id,
                shouldImprove = true,
                healthScore = health.score,
                reasons = health.issues,
                suggestions = buildSuggestions(health),
            )
        }
    }

    private fun buildSuggestions(health: DocumentHealth): List<String> = buildList {
        health.issues.forEach { issue ->
            when {
                "days" in issue -> add("Re-validate steps against the latest release and refresh the last-updated date")
                "outdated" in issue.lowercase() -> add("Reconcile with the current implementation before republishing")
                "thin" in issue.lowercase() -> add("Add prerequisites, edge cases and at least one concrete example")
                "TODO" in issue -> add("Resolve or remove unfinished TODO/TBD sections")
                "summary" in issue.lowercase() -> add("Write a one-paragraph summary so search and the assistant can ground answers")
                "review" in issue.lowercase() -> add("Address the pending review flags raised by the team")
                "Conflicting" in issue -> add("Merge the conflicting versions and keep a single source of truth")
                "source" in issue.lowercase() -> add("Re-link or import the deleted upstream source")
            }
        }
        if (isEmpty()) add("Run a full review of structure, accuracy and completeness")
    }
}
