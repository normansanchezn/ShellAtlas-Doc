package com.shelldocs.core.domain.usecase.document

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.repository.DocumentRepository

class GetDocumentDetailUseCase(private val documentRepository: DocumentRepository) {

    suspend operator fun invoke(id: String): DomainResult<Document> = documentRepository.document(id)
}
