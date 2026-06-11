package com.shelldocs.core.domain.repository

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.document.DocumentNode

/** Hierarchical explorer tree derived from document folder attributes. */
interface DocumentTreeRepository {

    suspend fun tree(): DomainResult<DocumentNode>
}
