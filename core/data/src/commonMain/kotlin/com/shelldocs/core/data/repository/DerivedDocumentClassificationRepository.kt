@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.core.data.repository

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.map
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.domain.entity.document.*
import com.shelldocs.core.domain.repository.DocumentClassificationRepository
import com.shelldocs.core.domain.repository.DocumentRepository

/**
 * Local heuristic stand-in for the "AI classification" model: infers missing
 * metadata from title/content keywords and existing attributes, scores a
 * confidence per attribute, and reports what still needs human review.
 */
class DerivedDocumentClassificationRepository(
    private val documentRepository: DocumentRepository,
    private val timeProvider: TimeProvider,
) : DocumentClassificationRepository {

    override suspend fun classify(documentId: String): DomainResult<DocumentClassificationResult> =
        documentRepository.document(documentId).map(::toClassificationResult)

    override suspend fun metadataIssues(): DomainResult<List<DocumentClassificationResult>> =
        documentRepository.documents().map { documents -> documents.map(::toClassificationResult) }

    override suspend fun acceptSuggestion(documentId: String, attribute: MetadataAttribute): DomainResult<Document> =
        when (val documentResult = documentRepository.document(documentId)) {
            is DomainResult.Failure -> documentResult
            is DomainResult.Success -> {
                val suggestion = toClassificationResult(documentResult.value).suggestions.firstOrNull { it.attribute == attribute }
                    ?: return DomainResult.failure(AppError.NotFound("No suggestion for ${attribute.displayName}"))
                assignMetadata(documentId, attribute, suggestion.suggestedValue)
            }
        }

    override suspend fun assignMetadata(documentId: String, attribute: MetadataAttribute, value: String): DomainResult<Document> =
        when (val documentResult = documentRepository.document(documentId)) {
            is DomainResult.Failure -> documentResult
            is DomainResult.Success -> {
                val updatedAttributes = documentResult.value.attributes.withValue(attribute, value)
                documentRepository.updateAttributes(documentId, updatedAttributes)
            }
        }

    private fun toClassificationResult(document: Document): DocumentClassificationResult {
        val attributes = document.attributes
        val suggestions = mutableListOf<MetadataSuggestion>()
        val missing = mutableListOf<MetadataAttribute>()

        if (attributes.owner.isBlank()) {
            missing += MetadataAttribute.OWNER
            inferOwner(document)?.let { suggestions += MetadataSuggestion(MetadataAttribute.OWNER, it, confidencePercent = 55) }
        }
        if (attributes.area == null) {
            missing += MetadataAttribute.AREA
            inferArea(document)?.let {
                suggestions += MetadataSuggestion(MetadataAttribute.AREA, it.displayName, confidencePercent = 78)
            }
        }
        if (attributes.applicationVersion.isBlank()) {
            missing += MetadataAttribute.APPLICATION_VERSION
            inferApplicationVersion(document)?.let {
                suggestions += MetadataSuggestion(MetadataAttribute.APPLICATION_VERSION, it, confidencePercent = 42)
            }
        }
        if (attributes.module.isBlank()) {
            missing += MetadataAttribute.MODULE
            inferModule(document)?.let { suggestions += MetadataSuggestion(MetadataAttribute.MODULE, it, confidencePercent = 64) }
        }
        if (attributes.platform.isBlank()) {
            missing += MetadataAttribute.PLATFORM
            inferPlatform(document)?.let { suggestions += MetadataSuggestion(MetadataAttribute.PLATFORM, it, confidencePercent = 88) }
        }

        val missingRequired = missing.filter { it.required }
        val missingOptional = missing.filterNot { it.required }
        return DocumentClassificationResult(
            documentId = document.id,
            documentTitle = document.title,
            status = MetadataClassificationStatus.fromMissingAttributes(missingRequired, missingOptional),
            missingAttributes = missing,
            suggestions = suggestions,
            sourceType = attributes.sourceType,
            classifiedAt = timeProvider.now(),
            area = attributes.area,
        )
    }

    private fun inferOwner(document: Document): String? = document.attributes.team.takeIf { it.isNotBlank() }

    private fun inferArea(document: Document) =
        Area.fromKey(document.attributes.team)
            ?: keywordMatch(
                document,
                mapOf(
                    "android" to Area.DEVELOPMENT,
                    "ios" to Area.DEVELOPMENT,
                    "api" to Area.DEVELOPMENT,
                    "server" to Area.DEVELOPMENT,
                    "design" to Area.DESIGN,
                    "ux" to Area.DESIGN,
                    "pipeline" to Area.MANAGEMENT,
                    "release" to Area.MANAGEMENT,
                    "architecture" to Area.ARCHITECTURE,
                    "shell" to Area.SHELL,
                    "business" to Area.BUSINESS,
                    "pi" to Area.PI,
                ),
            )

    private fun inferApplicationVersion(document: Document): String? =
        Regex("""\b\d+\.\d+(\.\d+)?\b""").find(document.title + " " + document.plainText)?.value

    private fun inferModule(document: Document): String? =
        keywordMatch(
            document,
            mapOf(
                "credential" to "Authentication",
                "auth" to "Authentication",
                "push" to "Notifications",
                "notification" to "Notifications",
                "session" to "Session Management",
                "payment" to "Payments",
            ),
        )

    private fun inferPlatform(document: Document): String? =
        keywordMatch(
            document,
            mapOf(
                "android" to "Android",
                "ios" to "iOS",
                "desktop" to "Desktop",
                "web" to "Web",
            ),
        )

    private fun <T> keywordMatch(document: Document, candidates: Map<String, T>): T? {
        val haystack = (document.title + " " + document.plainText).lowercase()
        return candidates.entries.firstOrNull { (keyword, _) -> keyword in haystack }?.value
    }

    private fun DocumentAttributes.withValue(attribute: MetadataAttribute, value: String): DocumentAttributes = when (attribute) {
        MetadataAttribute.OWNER -> copy(owner = value)
        MetadataAttribute.AREA -> copy(area = Area.fromKey(value))
        MetadataAttribute.APPLICATION_VERSION -> copy(applicationVersion = value)
        MetadataAttribute.MODULE -> copy(module = value)
        MetadataAttribute.PLATFORM -> copy(platform = value)
        MetadataAttribute.TEAM -> copy(team = value)
        MetadataAttribute.DOCUMENT_TYPE -> this
        MetadataAttribute.TAGS -> copy(tags = tags + value)
    }
}
