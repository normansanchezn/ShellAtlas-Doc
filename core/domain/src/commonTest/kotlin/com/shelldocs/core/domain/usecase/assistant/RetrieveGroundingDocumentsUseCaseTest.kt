package com.shelldocs.core.domain.usecase.assistant

import com.shelldocs.core.common.result.getOrDefault
import com.shelldocs.core.domain.fixtures.DocumentFixtures
import com.shelldocs.core.domain.fixtures.FakeDocumentRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RetrieveGroundingDocumentsUseCaseTest {

    private val repository = FakeDocumentRepository(
        stored = listOf(
            DocumentFixtures.document(id = "auth", title = "Authentication", tags = listOf("auth", "token")),
            DocumentFixtures.document(
                id = "loyalty",
                title = "Loyalty Rewards Flow",
                markdown = "# Loyalty\n\nPoints accrual, redemption, and tier progression logic.",
                tags = listOf("loyalty", "points"),
            ),
            DocumentFixtures.document(
                id = "release",
                title = "Release Process",
                markdown = "# Release\n\nStaging, QA sign-off and App Store submission.",
                tags = listOf("release"),
            ),
        ),
    )
    private val retrieve = RetrieveGroundingDocumentsUseCase(repository)

    @Test
    fun titleMatchesRankFirst() = runTest {
        val results = retrieve("How does the authentication token refresh work?").getOrDefault(emptyList())

        assertTrue(results.isNotEmpty())
        assertEquals("auth", results.first().document.id)
    }

    @Test
    fun unrelatedQuestionReturnsNoGrounding() = runTest {
        val results = retrieve("zzz qqq xxyy").getOrDefault(emptyList())
        assertTrue(results.isEmpty())
    }

    @Test
    fun limitIsRespected() = runTest {
        val results = retrieve("process flow points release authentication", limit = 2).getOrDefault(emptyList())
        assertTrue(results.size <= 2)
    }
}
