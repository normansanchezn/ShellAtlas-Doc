package com.shelldocs.core.domain.usecase.document

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.repository.DocumentRepository

class GetDocumentsUseCase(private val documentRepository: DocumentRepository) {

    suspend operator fun invoke(): DomainResult<List<Document>> = documentRepository.documents()
}
