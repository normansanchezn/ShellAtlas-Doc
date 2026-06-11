package com.shelldocs.core.domain.usecase.document

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.fixtures.DocumentFixtures
import com.shelldocs.core.domain.fixtures.FakeDocumentRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PermissionGatedDocumentUseCasesTest {

    private val repository = FakeDocumentRepository(stored = listOf(DocumentFixtures.document()))

    @Test
    fun businessRoleCannotCreateDocuments() = runTest {
        val result = CreateDocumentUseCase(repository)(
            actorRole = UserRole.BUSINESS,
            title = "New doc",
            markdown = "# New",
        )

        val failure = assertIs<DomainResult.Failure>(result)
        assertIs<AppError.Unauthorized>(failure.error)
    }

    @Test
    fun blankTitleIsRejectedEvenForOwners() = runTest {
        val result = CreateDocumentUseCase(repository)(
            actorRole = UserRole.OWNER,
            title = "   ",
            markdown = "# New",
        )

        val failure = assertIs<DomainResult.Failure>(result)
        assertIs<AppError.Validation>(failure.error)
    }

    @Test
    fun developCanPublishButNotDelete() = runTest {
        val publish = PublishDocumentUseCase(repository)(
            actorRole = UserRole.DEVELOP,
            id = "doc-1",
            markdown = "# Updated",
            changeSummary = "Refresh",
        )
        assertTrue(publish is DomainResult.Success)
        assertTrue(repository.publishedIds.contains("doc-1"))

        val delete = DeleteDocumentUseCase(repository)(actorRole = UserRole.DEVELOP, id = "doc-1")
        val failure = assertIs<DomainResult.Failure>(delete)
        assertIs<AppError.Unauthorized>(failure.error)
        assertTrue(repository.deletedIds.isEmpty())
    }

    @Test
    fun ownerCanDelete() = runTest {
        val result = DeleteDocumentUseCase(repository)(actorRole = UserRole.OWNER, id = "doc-1")

        assertTrue(result is DomainResult.Success)
        assertTrue(repository.deletedIds.contains("doc-1"))
    }

    @Test
    fun emptyPublishIsRejected() = runTest {
        val result = PublishDocumentUseCase(repository)(
            actorRole = UserRole.OWNER,
            id = "doc-1",
            markdown = "  ",
            changeSummary = "",
        )

        val failure = assertIs<DomainResult.Failure>(result)
        assertIs<AppError.Validation>(failure.error)
    }
}
