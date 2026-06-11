package com.shelldocs.core.data.repository

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.map
import com.shelldocs.core.domain.entity.document.DocumentNode
import com.shelldocs.core.domain.repository.DocumentRepository
import com.shelldocs.core.domain.repository.DocumentTreeRepository

/** Derives the explorer tree from the document list — no extra endpoint needed. */
class DerivedDocumentTreeRepository(
    private val documentRepository: DocumentRepository,
) : DocumentTreeRepository {

    override suspend fun tree(): DomainResult<DocumentNode> =
        documentRepository.documents().map(DocumentTreeBuilder::build)
}
