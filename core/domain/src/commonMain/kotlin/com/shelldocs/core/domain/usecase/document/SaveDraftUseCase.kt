package com.shelldocs.core.domain.usecase.document

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.document.DraftReceipt
import com.shelldocs.core.domain.repository.DocumentRepository

class SaveDraftUseCase(private val documentRepository: DocumentRepository) {

    suspend operator fun invoke(id: String, markdown: String): DomainResult<DraftReceipt> =
        documentRepository.saveDraft(id, markdown)
}
