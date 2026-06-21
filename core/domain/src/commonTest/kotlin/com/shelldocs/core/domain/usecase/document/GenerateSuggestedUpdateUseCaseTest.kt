@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.core.domain.usecase.document

import com.shelldocs.core.domain.entity.document.Area
import com.shelldocs.core.domain.entity.document.DocumentStatus
import com.shelldocs.core.domain.fixtures.DocumentFixtures
import com.shelldocs.core.domain.fixtures.FakeDocumentRepository
import com.shelldocs.core.domain.fixtures.FixedTimeProvider
import com.shelldocs.core.domain.usecase.assistant.EvaluateDocumentHealthUseCase
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GenerateSuggestedUpdateUseCaseTest {

    private val now = Instant.parse("2026-06-20T00:00:00Z")
    private val evaluateHealth = EvaluateDocumentHealthUseCase(FixedTimeProvider(now))

    @Test
    fun staleButOtherwiseValidDocumentReturnsNoContentChanges() = runTest {
        val document = DocumentFixtures.document(
            id = "stable-doc",
            title = "Stable Process",
            updatedAt = Instant.parse("2026-01-01T00:00:00Z"),
            markdown = """
                # Stable Process

                This guide describes the current process and already includes the required operational detail.

                ## Validation

                - Confirm the expected checks before release sign-off.
                - Escalate only if production monitoring detects regressions.
            """.trimIndent(),
        )
        val repository = FakeDocumentRepository(stored = listOf(document))
        val useCase = GenerateSuggestedUpdateUseCase(repository, evaluateHealth, FixedTimeProvider(now))

        val suggestion = useCase(document.id).getSuggestion()

        assertFalse(suggestion.hasSuggestedChanges)
        assertTrue(suggestion.suggestedMarkdown == suggestion.currentMarkdown)
    }

    @Test
    fun releaseDocumentProducesDocumentContentInsteadOfAuditBullets() = runTest {
        val eosb1 = DocumentFixtures.document(
            id = "eosb1",
            title = "EoSB1 Process for America's App - Android",
            status = DocumentStatus.UPDATES_PENDING,
            owner = "James O'Brien",
            module = "Release Process",
            team = "Android Shell App",
            platform = "Android",
            tags = listOf("eosb1", "release", "build", "pilot", "qa"),
            updatedAt = Instant.parse("2026-02-18T00:00:00Z"),
            markdown = """
                # EoSB1 Process for America's App - Android

                The Android EoSB1 process coordinates branch preparation, build generation and QA handoff for America's App.

                ## Branch Strategy

                - Prepare `develop`, `extra/pilot-8.99.0` and `madf/pilot`.
                - Validate pilot branch ownership before cutting a release candidate.

                ## Build Generation

                1. Update `build.gradle.kts` version values.
                2. Verify `updateconfig.py` and generated versionCodes.
                3. Run GitHub Actions release workflow for QA handoff.

                ## Validation

                - Confirm smoke tests on pilot branches.
                - Check Lokalise strings changes before sign-off.
                - Review Azure secrets required by the new build.

                TODO: document the newest rollback path after pilot rejection.
            """.trimIndent(),
        ).copy(
            attributes = DocumentFixtures.document().attributes.copy(
                owner = "James O'Brien",
                module = "Release Process",
                team = "Android Shell App",
                platform = "Android",
                tags = listOf("eosb1", "release", "build", "pilot", "qa"),
                area = Area.DEVELOPMENT,
                applicationVersion = "9.5.0",
            ),
        )

        val releaseProcess = DocumentFixtures.document(
            id = "release-process",
            title = "Release Process",
            module = "Release Process",
            team = "Platform Team",
            platform = "Process",
            tags = listOf("release", "qa", "release-notes"),
            updatedAt = now,
        ).copy(
            attributes = DocumentFixtures.document().attributes.copy(
                owner = "Sofia Reyes",
                module = "Release Process",
                team = "Platform Team",
                platform = "Process",
                tags = listOf("release", "qa", "release-notes"),
                area = Area.DEVELOPMENT,
                applicationVersion = "9.6.0",
            ),
        )

        val lokalise = DocumentFixtures.document(
            id = "lokalise",
            title = "Lokalise Strings Update Process",
            module = "Localization",
            team = "Platform Team",
            platform = "Cross-platform",
            tags = listOf("lokalise", "localization"),
            updatedAt = now,
        ).copy(
            attributes = DocumentFixtures.document().attributes.copy(
                owner = "Sofia Reyes",
                module = "Localization",
                team = "Platform Team",
                platform = "Cross-platform",
                tags = listOf("lokalise", "localization"),
                area = Area.DEVELOPMENT,
                applicationVersion = "9.6.0",
            ),
        )

        val secrets = DocumentFixtures.document(
            id = "azure-secrets",
            title = "Azure Secrets Management for Mobile",
            module = "Platform Security",
            team = "Platform Team",
            platform = "Cross-platform",
            tags = listOf("azure secrets", "environment values"),
            updatedAt = now,
        ).copy(
            attributes = DocumentFixtures.document().attributes.copy(
                owner = "Sofia Reyes",
                module = "Platform Security",
                team = "Platform Team",
                platform = "Cross-platform",
                tags = listOf("azure secrets", "environment values"),
                area = Area.DEVELOPMENT,
                applicationVersion = "9.6.0",
            ),
        )

        val repository = FakeDocumentRepository(stored = listOf(eosb1, releaseProcess, lokalise, secrets))
        val useCase = GenerateSuggestedUpdateUseCase(repository, evaluateHealth, FixedTimeProvider(now))

        val suggestion = useCase(eosb1.id).getSuggestion()

        assertTrue(suggestion.hasSuggestedChanges)
        assertTrue("## Rollback Process" in suggestion.suggestedMarkdown)
        assertTrue("Validate release configuration against application version 9.6.0." in suggestion.suggestedMarkdown)
        assertFalse("## AI Suggested Update" in suggestion.suggestedMarkdown)
        assertFalse("needs review" in suggestion.suggestedMarkdown.lowercase())
        assertFalse("Not updated in" in suggestion.suggestedMarkdown)
    }
}

private fun com.shelldocs.core.common.result.DomainResult<com.shelldocs.core.domain.entity.document.DocumentUpdateSuggestion>.getSuggestion() =
    (this as com.shelldocs.core.common.result.DomainResult.Success).value
