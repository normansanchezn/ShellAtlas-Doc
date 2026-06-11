package com.shelldocs.core.domain.usecase.document

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.document.DocumentVersion
import com.shelldocs.core.domain.repository.DocumentRepository

class GetDocumentVersionsUseCase(private val documentRepository: DocumentRepository) {

    suspend operator fun invoke(id: String): DomainResult<List<DocumentVersion>> =
        documentRepository.versions(id)
}
