package com.shelldocs.core.domain.usecase.assistant

import com.shelldocs.core.domain.entity.document.DocumentStatus
import com.shelldocs.core.domain.fixtures.DocumentFixtures
import com.shelldocs.core.domain.fixtures.FixedTimeProvider
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EvaluateDocumentHealthUseCaseTest {

    private val now = Instant.parse("2026-06-11T00:00:00Z")
    private val evaluate = EvaluateDocumentHealthUseCase(FixedTimeProvider(now))
    private val completeMarkdown = """
        # Authentication

        Access tokens expire after 60 minutes and are refreshed silently before the user notices.
        The refresh flow rotates credentials, preserves session continuity and records failures for later review.

        ## Lifecycle

        1. Detect that the active token is close to expiry.
        2. Request a new token pair from the authorization server.
        3. Persist the rotated credentials in secure local storage.
        4. Resume the blocked requests with the refreshed token.

        ## Notes

        This document includes prerequisites, edge cases, fallback behavior and recovery details so it remains actionable during release support and incident response.
    """.trimIndent()

    @Test
    fun freshPublishedDocumentIsHealthy() {
        val document = DocumentFixtures.document(
            updatedAt = Instant.parse("2026-06-01T00:00:00Z"),
            markdown = completeMarkdown,
        )

        val health = evaluate(document)

        assertEquals(100, health.score)
        assertTrue(health.isHealthy)
        assertTrue(health.issues.isEmpty())
    }

    @Test
    fun veryStaleDocumentLosesThirtyFivePoints() {
        val document = DocumentFixtures.document(
            updatedAt = Instant.parse("2025-12-01T00:00:00Z"),
            markdown = completeMarkdown,
        )

        val health = evaluate(document)

        assertEquals(65, health.score)
        assertFalse(health.isHealthy)
        assertTrue(health.issues.any { "Not updated" in it })
    }

    @Test
    fun outdatedStatusAndTodoMarkersStack() {
        val document = DocumentFixtures.document(
            status = DocumentStatus.OUTDATED,
            markdown = "# Doc\n\nTODO: rewrite this whole section with the new payment gateway details and flows.",
            updatedAt = now,
        )

        val health = evaluate(document)

        assertTrue(health.issues.any { "outdated" in it.lowercase() })
        assertTrue(health.issues.any { "TODO" in it })
        assertTrue(health.score <= 60)
    }

    @Test
    fun scoreNeverGoesBelowZero() {
        val document = DocumentFixtures.document(
            status = DocumentStatus.OUTDATED,
            summary = "",
            markdown = "TODO",
            updatedAt = Instant.parse("2024-01-01T00:00:00Z"),
        )

        val health = evaluate(document)

        assertTrue(health.score >= 0)
    }
}
