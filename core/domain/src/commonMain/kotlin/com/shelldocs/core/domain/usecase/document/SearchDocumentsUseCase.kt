package com.shelldocs.core.domain.usecase.document

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.repository.DocumentRepository

class SearchDocumentsUseCase(private val documentRepository: DocumentRepository) {

    suspend operator fun invoke(query: String): DomainResult<List<Document>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return DomainResult.success(emptyList())
        return documentRepository.search(trimmed)
    }
}
