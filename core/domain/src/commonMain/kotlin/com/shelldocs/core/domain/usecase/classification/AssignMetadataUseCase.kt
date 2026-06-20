package com.shelldocs.core.domain.usecase.classification

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.MetadataAttribute
import com.shelldocs.core.domain.repository.DocumentClassificationRepository

/** Admin manually edits a suggestion or assigns a value the AI could not infer. */
class AssignMetadataUseCase(private val classificationRepository: DocumentClassificationRepository) {

    suspend operator fun invoke(documentId: String, attribute: MetadataAttribute, value: String): DomainResult<Document> =
        classificationRepository.assignMetadata(documentId, attribute, value)
}
