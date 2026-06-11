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
            DocumentFixtures.document(
                id = "eosb1",
                title = "EoSB1 Process for America's App - Android",
                summary = "Android release build process for EoSB1.",
                tags = listOf("release", "build", "eosb1"),
                platform = "Android",
                module = "Release Process",
            ),
            DocumentFixtures.document(
                id = "lokalise",
                title = "Lokalise Strings Update Process",
                summary = "Localization sync workflow and translations release checks.",
                tags = listOf("lokalise", "localization", "translations"),
                module = "Localization",
                platform = "Cross-platform",
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

    @Test
    fun aliasExpansionImprovesReleaseGrounding() = runTest {
        val results = retrieve("release build").getOrDefault(emptyList())

        assertEquals("eosb1", results.firstOrNull()?.document?.id)
    }

    @Test
    fun aliasExpansionImprovesLocalizationGrounding() = runTest {
        val results = retrieve("localization").getOrDefault(emptyList())

        assertEquals("lokalise", results.firstOrNull()?.document?.id)
    }
}
