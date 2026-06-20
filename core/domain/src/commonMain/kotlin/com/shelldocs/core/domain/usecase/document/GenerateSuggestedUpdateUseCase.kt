@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.core.domain.usecase.document

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.map
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.domain.entity.assistant.DocumentHealth
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.DocumentUpdateSuggestion
import com.shelldocs.core.domain.entity.document.LineOrigin
import com.shelldocs.core.domain.entity.document.SuggestionLine
import com.shelldocs.core.domain.repository.DocumentRepository
import com.shelldocs.core.domain.usecase.assistant.EvaluateDocumentHealthUseCase

/**
 * Local-AI stand-in for the Documentation Health "Update" action: runs the
 * deterministic health audit and proposes a reviewable addendum instead of
 * silently rewriting the document — precision over creativity, per spec.
 * Preserves the original markdown verbatim; only the appended lines are
 * flagged [LineOrigin.AI_SUGGESTED].
 */
class GenerateSuggestedUpdateUseCase(
    private val documentRepository: DocumentRepository,
    private val evaluateHealth: EvaluateDocumentHealthUseCase,
    private val timeProvider: TimeProvider,
) {

    suspend operator fun invoke(documentId: String): DomainResult<DocumentUpdateSuggestion> =
        documentRepository.document(documentId).map { document -> toSuggestion(document) }

    private fun toSuggestion(document: Document): DocumentUpdateSuggestion {
        val health = evaluateHealth(document)
        val originalLines = document.rawMarkdown.lines().map { SuggestionLine(it, LineOrigin.ORIGINAL) }
        val suggestedLines = originalLines + addendumLines(health)
        return DocumentUpdateSuggestion(
            documentId = document.id,
            documentTitle = document.title,
            ownerName = document.attributes.owner,
            currentMarkdown = document.rawMarkdown,
            lines = suggestedLines,
            generatedAt = timeProvider.now(),
        )
    }

    private fun addendumLines(health: DocumentHealth): List<SuggestionLine> {
        if (health.issues.isEmpty()) {
            return listOf(
                SuggestionLine("", LineOrigin.ORIGINAL),
                SuggestionLine("_Automatic review found no outdated sections._", LineOrigin.AI_SUGGESTED),
            )
        }
        return listOf(
            SuggestionLine("", LineOrigin.ORIGINAL),
            SuggestionLine("## AI Suggested Update", LineOrigin.AI_SUGGESTED),
        ) + health.issues.map { issue -> SuggestionLine("- $issue — needs review.", LineOrigin.AI_SUGGESTED) }
    }
}
