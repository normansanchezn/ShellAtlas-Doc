package com.shelldocs.core.domain.repository

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.DocumentClassificationResult
import com.shelldocs.core.domain.entity.document.MetadataAttribute

/** Automatic metadata classification feed for the Metadata Issues screen. */
interface DocumentClassificationRepository {

    suspend fun classify(documentId: String): DomainResult<DocumentClassificationResult>

    suspend fun metadataIssues(): DomainResult<List<DocumentClassificationResult>>

    suspend fun acceptSuggestion(documentId: String, attribute: MetadataAttribute): DomainResult<Document>

    suspend fun assignMetadata(documentId: String, attribute: MetadataAttribute, value: String): DomainResult<Document>
}
