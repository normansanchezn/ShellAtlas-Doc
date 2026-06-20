@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.core.domain.usecase.assistant

import com.shelldocs.core.domain.entity.document.DocumentStatus
import com.shelldocs.core.domain.fixtures.DocumentFixtures
import com.shelldocs.core.domain.fixtures.FixedTimeProvider
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShouldImproveDocumentUseCaseTest {

    private val now = Instant.parse("2026-06-11T00:00:00Z")
    private val decide = ShouldImproveDocumentUseCase(EvaluateDocumentHealthUseCase(FixedTimeProvider(now)))

    @Test
    fun healthyDocumentIsExplicitlyLeftAlone() {
        val decision = decide(DocumentFixtures.document(updatedAt = now))

        assertFalse(decision.shouldImprove)
        assertTrue(decision.reasons.any { "threshold" in it })
        assertTrue(decision.suggestions.isEmpty())
    }

    @Test
    fun unhealthyDocumentGetsActionableSuggestions() {
        val decision = decide(
            DocumentFixtures.document(
                status = DocumentStatus.OUTDATED,
                updatedAt = Instant.parse("2025-10-01T00:00:00Z"),
            ),
        )

        assertTrue(decision.shouldImprove)
        assertTrue(decision.reasons.isNotEmpty())
        assertTrue(decision.suggestions.isNotEmpty())
    }
}
