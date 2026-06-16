@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.core.data.repository

import com.shelldocs.core.common.result.getOrNull
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.data.demo.DemoDocumentRepository
import com.shelldocs.core.data.demo.DemoKnowledgeCheckpointRepository
import com.shelldocs.core.data.demo.DemoSeed
import com.shelldocs.core.domain.usecase.assistant.EvaluateDocumentHealthUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DerivedDashboardRepositoryTest {

    private val timeProvider = TimeProvider { DemoSeed.now }
    private val repository = DerivedDashboardRepository(
        documentRepository = DemoDocumentRepository(timeProvider),
        evaluateHealth = EvaluateDocumentHealthUseCase(timeProvider),
        knowledgeCheckpointRepository = DemoKnowledgeCheckpointRepository(),
    )

    @Test
    fun countsComeFromTheCorpus() = runTest {
        val metrics = repository.metrics().getOrNull()

        assertNotNull(metrics)
        assertEquals(DemoSeed.documents.size, metrics.totalDocuments)
        assertEquals(1, metrics.outdatedDocuments)
    }

    @Test
    fun statusBreakdownPercentagesAreConsistent() = runTest {
        val metrics = repository.metrics().getOrNull()!!
        val breakdown = metrics.statusBreakdown

        val sum = breakdown.publishedPercent + breakdown.outdatedPercent +
            breakdown.draftPercent + breakdown.pendingPercent
        assertTrue(sum in 90..100, "rounded percentages should be close to 100, got $sum")
    }

    @Test
    fun moduleCoverageCoversEveryModule() = runTest {
        val metrics = repository.metrics().getOrNull()!!
        val modules = DemoSeed.documents.map { it.attributes.module }.toSet()

        assertEquals(modules.size, metrics.moduleCoverage.size)
        metrics.moduleCoverage.forEach { assertTrue(it.coveragePercent in 0..100) }
    }

    @Test
    fun knowledgeHealthIsWithinBounds() = runTest {
        val metrics = repository.metrics().getOrNull()!!
        assertTrue(metrics.knowledgeHealthScore in 0..100)
    }
}
