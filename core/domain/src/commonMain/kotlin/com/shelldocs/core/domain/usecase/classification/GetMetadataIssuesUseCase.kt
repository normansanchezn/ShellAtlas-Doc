package com.shelldocs.core.domain.usecase.classification

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.map
import com.shelldocs.core.domain.entity.document.DocumentClassificationResult
import com.shelldocs.core.domain.entity.document.MetadataClassificationStatus
import com.shelldocs.core.domain.repository.DocumentClassificationRepository

class GetMetadataIssuesUseCase(private val classificationRepository: DocumentClassificationRepository) {

    suspend operator fun invoke(): DomainResult<List<DocumentClassificationResult>> =
        classificationRepository.metadataIssues().map { results ->
            results
                .filter { it.status != MetadataClassificationStatus.READY }
                .sortedWith(compareBy({ -it.status.ordinal }, { it.documentTitle.lowercase() }))
        }
}
