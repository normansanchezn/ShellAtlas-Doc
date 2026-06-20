package com.shelldocs.core.domain.usecase.classification

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.MetadataAttribute
import com.shelldocs.core.domain.repository.DocumentClassificationRepository

/** Admin accepts an AI-suggested value for a missing/low-confidence attribute as-is. */
class AcceptMetadataSuggestionUseCase(private val classificationRepository: DocumentClassificationRepository) {

    suspend operator fun invoke(documentId: String, attribute: MetadataAttribute): DomainResult<Document> =
        classificationRepository.acceptSuggestion(documentId, attribute)
}
