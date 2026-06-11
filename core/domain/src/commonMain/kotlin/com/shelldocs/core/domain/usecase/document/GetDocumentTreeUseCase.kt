package com.shelldocs.core.domain.usecase.document

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.document.DocumentNode
import com.shelldocs.core.domain.repository.DocumentTreeRepository

class GetDocumentTreeUseCase(private val treeRepository: DocumentTreeRepository) {

    suspend operator fun invoke(): DomainResult<DocumentNode> = treeRepository.tree()
}
