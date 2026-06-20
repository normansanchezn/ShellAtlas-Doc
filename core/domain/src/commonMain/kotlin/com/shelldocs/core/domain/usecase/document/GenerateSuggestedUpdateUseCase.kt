@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.core.domain.usecase.document

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.map
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.domain.entity.assistant.DocumentHealth
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.DocumentUpdateSuggestion
import com.shelldocs.core.domain.repository.DocumentRepository
import com.shelldocs.core.domain.usecase.assistant.EvaluateDocumentHealthUseCase

/**
 * Local-AI stand-in for the Documentation Health "Update" action: runs the
 * deterministic health audit and proposes a reviewable addendum instead of
 * silently rewriting the document — precision over creativity, per spec.
 * Preserves the original markdown verbatim; only appends a flagged section,
 * loaded as one continuous string into the suggested-update editor.
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
        val addendum = addendumText(health)
        return DocumentUpdateSuggestion(
            documentId = document.id,
            documentTitle = document.title,
            attributes = document.attributes,
            currentContentBlocks = document.content.blocks,
            currentMarkdown = document.rawMarkdown,
            suggestedMarkdown = if (addendum.isBlank()) document.rawMarkdown else "${document.rawMarkdown}\n\n$addendum",
            generatedAt = timeProvider.now(),
        )
    }

    private fun addendumText(health: DocumentHealth): String {
        if (health.issues.isEmpty()) return ""
        return "## AI Suggested Update\n" + health.issues.joinToString("\n") { issue -> "- $issue — needs review." }
    }
}
