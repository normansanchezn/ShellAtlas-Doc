package com.shelldocs.core.domain.usecase.assistant

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.map
import com.shelldocs.core.domain.entity.assistant.ScoredDocument
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.repository.DocumentRepository
import com.shelldocs.core.domain.usecase.document.KnowledgeQueryExpander

/**
 * Keyword-overlap retrieval that selects the documents an answer will be
 * grounded on. Title and tag hits weigh more than body hits.
 */
class RetrieveGroundingDocumentsUseCase(private val documentRepository: DocumentRepository) {

    suspend operator fun invoke(question: String, limit: Int = DEFAULT_LIMIT): DomainResult<List<ScoredDocument>> {
        val terms = KnowledgeQueryExpander.expandedTerms(question)
        return documentRepository.documents().map { documents ->
            documents
                .map { ScoredDocument(it, score(it, terms)) }
                .filter { it.score > 0.0 }
                .sortedByDescending { it.score }
                .take(limit)
        }
    }

    private fun score(document: Document, terms: List<String>): Double {
        if (terms.isEmpty()) return 0.0
        val title = document.title.lowercase()
        val tags = document.attributes.tags.joinToString(" ").lowercase()
        val body = document.plainText.lowercase()
        var hits = 0.0
        terms.forEach { term ->
            if (term in title) hits += TITLE_WEIGHT
            if (term in tags) hits += TAG_WEIGHT
            if (term in body) hits += BODY_WEIGHT
        }
        return hits / (terms.size * TITLE_WEIGHT)
    }

    private companion object {
        const val DEFAULT_LIMIT = 5
        const val TITLE_WEIGHT = 3.0
        const val TAG_WEIGHT = 2.0
        const val BODY_WEIGHT = 1.0
    }
}
