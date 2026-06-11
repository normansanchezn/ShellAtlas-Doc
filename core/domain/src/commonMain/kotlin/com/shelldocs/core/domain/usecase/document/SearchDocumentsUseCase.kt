package com.shelldocs.core.domain.usecase.document

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.map
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.repository.DocumentRepository

class SearchDocumentsUseCase(private val documentRepository: DocumentRepository) {

    suspend operator fun invoke(query: String): DomainResult<List<Document>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return DomainResult.success(emptyList())
        val terms = KnowledgeQueryExpander.expandedTerms(trimmed)
        return documentRepository.documents().map { documents ->
            documents
                .map { document -> document to score(document, trimmed.lowercase(), terms) }
                .filter { (_, score) -> score > 0 }
                .sortedWith(compareByDescending<Pair<Document, Int>> { it.second }.thenBy { it.first.title.lowercase() })
                .map { it.first }
        }
    }

    private fun score(document: Document, query: String, terms: List<String>): Int {
        val title = document.title.lowercase()
        val summary = document.summary.lowercase()
        val body = document.plainText.lowercase()
        val tags = document.attributes.tags.map(String::lowercase)
        val module = document.attributes.module.lowercase()
        val team = document.attributes.team.lowercase()
        val platform = document.attributes.platform.lowercase()

        var score = 0
        if (query in title) score += TITLE_WEIGHT + EXACT_QUERY_BONUS
        if (query in summary) score += SUMMARY_WEIGHT + EXACT_QUERY_BONUS
        if (query in body) score += BODY_WEIGHT + EXACT_QUERY_BONUS

        terms.forEach { term ->
            if (term in title) score += TITLE_WEIGHT
            if (term in summary) score += SUMMARY_WEIGHT
            if (tags.any { term in it }) score += TAG_WEIGHT
            if (term in module) score += METADATA_WEIGHT
            if (term in team) score += METADATA_WEIGHT
            if (term in platform) score += METADATA_WEIGHT
            if (term in body) score += BODY_WEIGHT
        }

        return score
    }

    private companion object {
        const val TITLE_WEIGHT = 6
        const val SUMMARY_WEIGHT = 4
        const val TAG_WEIGHT = 4
        const val METADATA_WEIGHT = 3
        const val BODY_WEIGHT = 2
        const val EXACT_QUERY_BONUS = 2
    }
}
