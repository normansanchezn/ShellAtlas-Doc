package com.shelldocs.core.domain.usecase.classification

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.document.DocumentClassificationResult
import com.shelldocs.core.domain.repository.DocumentClassificationRepository

class ClassifyDocumentUseCase(private val classificationRepository: DocumentClassificationRepository) {

    suspend operator fun invoke(documentId: String): DomainResult<DocumentClassificationResult> =
        classificationRepository.classify(documentId)
}
