package com.shelldocs.core.domain.usecase.classification

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.MetadataAssignment
import com.shelldocs.core.domain.repository.DocumentClassificationRepository

class ApplyMetadataAssignmentsUseCase(
    private val classificationRepository: DocumentClassificationRepository,
) {

    suspend operator fun invoke(
        documentId: String,
        assignments: List<MetadataAssignment>,
    ): DomainResult<Document> {
        val normalized = assignments
            .filter { it.value.isNotBlank() }
            .distinctBy { it.attribute }

        if (normalized.isEmpty()) {
            return DomainResult.failure(AppError.Validation("No metadata changes were provided"))
        }

        var lastUpdated: Document? = null
        normalized.forEach { assignment ->
            when (
                val result = classificationRepository.assignMetadata(
                    documentId = documentId,
                    attribute = assignment.attribute,
                    value = assignment.value,
                )
            ) {
                is DomainResult.Failure -> return result
                is DomainResult.Success -> lastUpdated = result.value
            }
        }

        return lastUpdated?.let(DomainResult.Companion::success)
            ?: DomainResult.failure(AppError.Unknown("Metadata update completed without a document result"))
    }
}
