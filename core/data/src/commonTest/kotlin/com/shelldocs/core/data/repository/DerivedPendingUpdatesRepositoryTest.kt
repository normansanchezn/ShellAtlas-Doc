@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.core.data.repository

import com.shelldocs.core.common.result.getOrDefault
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.data.demo.DemoDocumentRepository
import com.shelldocs.core.data.demo.DemoSeed
import com.shelldocs.core.domain.entity.updates.RiskLevel
import com.shelldocs.core.domain.usecase.assistant.EvaluateDocumentHealthUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DerivedPendingUpdatesRepositoryTest {

    private val timeProvider = TimeProvider { DemoSeed.now }
    private val repository = DerivedPendingUpdatesRepository(
        documentRepository = DemoDocumentRepository(timeProvider),
        evaluateHealth = EvaluateDocumentHealthUseCase(timeProvider),
        timeProvider = timeProvider,
    )

    @Test
    fun onlyUnhealthyDocumentsAppearInTriage() = runTest {
        val updates = repository.pendingUpdates().getOrDefault(emptyList())

        assertTrue(updates.isNotEmpty())
        assertTrue(updates.none { it.documentId == "doc-authentication" }, "fresh doc must not be triaged")
        assertTrue(updates.any { it.documentId == "doc-loyalty" }, "outdated stale doc must be triaged")
        assertTrue(updates.any { it.documentId == "doc-eosb1" }, "EoSB1 should be triaged from recovered ShellDoc demo data")
    }

    @Test
    fun impactIsInverseOfHealthAndRiskIsOnlyCriticalOrLowByDefault() = runTest {
        val updates = repository.pendingUpdates().getOrDefault(emptyList())

        updates.forEach { update ->
            assertTrue(update.impactScore in 31..100)
            assertTrue(update.risk == RiskLevel.CRITICAL || update.risk == RiskLevel.LOW)
        }
    }

    @Test
    fun manualOverrideTakesPrecedenceOverAutoRisk() = runTest {
        val before = repository.pendingUpdates().getOrDefault(emptyList()).first()

        val updated = repository.setManualRisk(before.documentId, RiskLevel.MEDIUM).getOrDefault(before)
        assertEquals(RiskLevel.MEDIUM, updated.risk)
        assertEquals(RiskLevel.MEDIUM, updated.manualRiskOverride)

        val cleared = repository.setManualRisk(before.documentId, null).getOrDefault(updated)
        assertEquals(before.risk, cleared.risk)
        assertEquals(null, cleared.manualRiskOverride)
    }

    @Test
    fun ageInDaysIsComputedFromLastUpdate() = runTest {
        val updates = repository.pendingUpdates().getOrDefault(emptyList())
        val loyalty = updates.first { it.documentId == "doc-loyalty" }

        assertTrue(loyalty.ageDays >= 150, "expected ~150 days, got ${loyalty.ageDays}")
    }
}
