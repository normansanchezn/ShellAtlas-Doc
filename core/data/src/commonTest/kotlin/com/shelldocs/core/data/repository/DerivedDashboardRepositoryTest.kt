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
        knowledgeCheckpointRepository = DemoKnowledgeCheckpointRepository(timeProvider),
        conversationRepository = InMemoryConversationRepository(),
    )

    @Test
    fun healthyAndAttentionCountsCoverTheWholeCorpus() = runTest {
        val metrics = repository.metrics().getOrNull()

        assertNotNull(metrics)
        assertEquals(DemoSeed.documents.size, metrics.healthyDocuments + metrics.attentionDocuments)
    }

    @Test
    fun statusBreakdownPercentagesAreConsistent() = runTest {
        val metrics = repository.metrics().getOrNull()!!

        val sum = metrics.statusBreakdown.sumOf { it.percent }
        assertTrue(sum in 90..100, "rounded percentages should be close to 100, got $sum")
    }

    @Test
    fun areaCoverageCoversEveryAssignedArea() = runTest {
        val metrics = repository.metrics().getOrNull()!!
        val areas = DemoSeed.documents.mapNotNull { it.attributes.area }.toSet()

        assertEquals(
            areas.size + if (DemoSeed.documents.any { it.attributes.area == null }) 1 else 0,
            metrics.areaCoverage.size
        )
        metrics.areaCoverage.forEach { assertTrue(it.healthyPercent in 0..100) }
    }

    @Test
    fun knowledgeTransferPercentIsWithinBounds() = runTest {
        val metrics = repository.metrics().getOrNull()!!
        assertTrue(metrics.knowledgeTransferPercent in 0..100)
    }
}
