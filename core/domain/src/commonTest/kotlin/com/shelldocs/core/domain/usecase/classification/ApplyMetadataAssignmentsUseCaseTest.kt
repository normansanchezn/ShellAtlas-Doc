@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.core.domain.usecase.classification

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.document.*
import com.shelldocs.core.domain.repository.DocumentClassificationRepository
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

private class RecordingClassificationRepository : DocumentClassificationRepository {
    val assignments = mutableListOf<MetadataAssignment>()

    override suspend fun classify(documentId: String): DomainResult<DocumentClassificationResult> =
        DomainResult.failure(AppError.NotFound("Unused"))

    override suspend fun metadataIssues(): DomainResult<List<DocumentClassificationResult>> =
        DomainResult.failure(AppError.NotFound("Unused"))

    override suspend fun acceptSuggestion(documentId: String, attribute: MetadataAttribute): DomainResult<Document> =
        DomainResult.failure(AppError.NotFound("Unused"))

    override suspend fun assignMetadata(
        documentId: String,
        attribute: MetadataAttribute,
        value: String
    ): DomainResult<Document> {
        assignments += MetadataAssignment(attribute, value)
        return DomainResult.success(sampleDocument())
    }
}

class ApplyMetadataAssignmentsUseCaseTest {

    @Test
    fun appliesUniqueNonBlankAssignmentsInOrder() = kotlinx.coroutines.test.runTest {
        val repository = RecordingClassificationRepository()
        val useCase = ApplyMetadataAssignmentsUseCase(repository)

        val result = useCase(
            documentId = "doc-1",
            assignments = listOf(
                MetadataAssignment(MetadataAttribute.OWNER, "Norman Sanchez"),
                MetadataAssignment(MetadataAttribute.OWNER, "Duplicate"),
                MetadataAssignment(MetadataAttribute.AREA, "Development"),
                MetadataAssignment(MetadataAttribute.TAGS, "android, release"),
                MetadataAssignment(MetadataAttribute.MODULE, " "),
            ),
        )

        assertIs<DomainResult.Success<Document>>(result)
        assertEquals(
            listOf(
                MetadataAssignment(MetadataAttribute.OWNER, "Norman Sanchez"),
                MetadataAssignment(MetadataAttribute.AREA, "Development"),
                MetadataAssignment(MetadataAttribute.TAGS, "android, release"),
            ),
            repository.assignments,
        )
    }

    @Test
    fun rejectsEmptyAssignmentList() = kotlinx.coroutines.test.runTest {
        val repository = RecordingClassificationRepository()
        val useCase = ApplyMetadataAssignmentsUseCase(repository)

        val result = useCase(
            documentId = "doc-1",
            assignments = listOf(MetadataAssignment(MetadataAttribute.OWNER, " ")),
        )

        val failure = assertIs<DomainResult.Failure>(result)
        assertTrue(failure.error is AppError.Validation)
        assertTrue(repository.assignments.isEmpty())
    }
}

private fun sampleDocument() = Document(
    id = "doc-1",
    title = "Sample",
    summary = "Summary",
    status = DocumentStatus.PUBLISHED,
    classification = DocumentClassification.INTERNAL,
    rawMarkdown = "Content",
    content = DocumentContent(),
    plainText = "Content",
    attributes = DocumentAttributes(),
    createdAt = Instant.parse("2026-06-20T00:00:00Z"),
    updatedAt = Instant.parse("2026-06-20T00:00:00Z"),
)
