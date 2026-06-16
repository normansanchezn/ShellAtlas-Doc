@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.core.data.assistant

import com.shelldocs.core.common.result.getOrNull
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.data.demo.DemoSeed
import com.shelldocs.core.domain.entity.assistant.AnswerConfidence
import com.shelldocs.core.domain.entity.assistant.AssistantIntentType
import com.shelldocs.core.domain.entity.assistant.ScoredDocument
import com.shelldocs.core.domain.usecase.assistant.EvaluateDocumentHealthUseCase
import com.shelldocs.core.domain.usecase.assistant.ShouldImproveDocumentUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GroundedAssistantEngineTest {

    private val timeProvider = TimeProvider { DemoSeed.now }
    private val engine = GroundedAssistantEngine(
        shouldImproveDocument = ShouldImproveDocumentUseCase(EvaluateDocumentHealthUseCase(timeProvider)),
    )

    private val authDoc = DemoSeed.documents.first { it.id == "doc-authentication" }
    private val eosbDoc = DemoSeed.documents.first { it.id == "doc-release-process" }
    private val loyaltyDoc = DemoSeed.documents.first { it.id == "doc-loyalty" }

    @Test
    fun questionAnswersQuoteRelevantPassages() = runTest {
        val answer = engine.answer(
            question = "What happens when tokens expire?",
            intent = AssistantIntentType.QUESTION,
            grounding = listOf(ScoredDocument(authDoc, 0.8)),
        ).getOrNull()

        assertNotNull(answer)
        assertTrue("Authentication" in answer.markdown)
        assertEquals(AnswerConfidence.HIGH, answer.confidence)
        assertEquals(1, answer.sources.size)
        assertEquals(80, answer.sources.first().relevance)
    }

    @Test
    fun explainFlowProducesNumberedSteps() = runTest {
        val answer = engine.answer(
            question = "Explain the authentication flow",
            intent = AssistantIntentType.EXPLAIN_FLOW,
            grounding = listOf(ScoredDocument(authDoc, 0.7)),
        ).getOrNull()

        assertNotNull(answer)
        assertTrue("step by step" in answer.markdown)
        assertTrue("1. **" in answer.markdown)
        assertTrue("Token Lifecycle" in answer.markdown)
    }

    @Test
    fun processQuestionsPreferFlowchartOverTimelineDiagram() = runTest {
        val answer = engine.answer(
            question = "Explica el flujo del proceso EoSB1 paso a paso",
            intent = AssistantIntentType.EXPLAIN_FLOW,
            grounding = listOf(ScoredDocument(eosbDoc, 0.9)),
        ).getOrNull()

        assertNotNull(answer)
        assertTrue("```mermaid\nflowchart TD" in answer.markdown)
        assertTrue("gantt" !in answer.markdown)
    }

    @Test
    fun healthyDocumentIsNotRewritten() = runTest {
        val freshDoc = authDoc.copy(updatedAt = DemoSeed.now)
        val answer = engine.answer(
            question = "Should we improve the authentication doc?",
            intent = AssistantIntentType.IMPROVE_DOCUMENT,
            grounding = listOf(ScoredDocument(freshDoc, 0.9)),
        ).getOrNull()

        assertNotNull(answer)
        assertTrue("would *not* rewrite" in answer.markdown)
    }

    @Test
    fun staleDocumentGetsImprovementPlan() = runTest {
        val answer = engine.answer(
            question = "Should we improve the loyalty doc?",
            intent = AssistantIntentType.IMPROVE_DOCUMENT,
            grounding = listOf(ScoredDocument(loyaltyDoc, 0.9)),
        ).getOrNull()

        assertNotNull(answer)
        assertTrue("does need an update" in answer.markdown)
        assertTrue("Suggested improvements:" in answer.markdown)
    }

    @Test
    fun emptyGroundingYieldsNotEnoughInformation() = runTest {
        val answer = engine.answer(
            question = "Tell me about quantum widgets",
            intent = AssistantIntentType.QUESTION,
            grounding = emptyList(),
        ).getOrNull()

        assertNotNull(answer)
        assertEquals(AnswerConfidence.NOT_ENOUGH_INFORMATION, answer.confidence)
        assertTrue(answer.sources.isEmpty())
    }

    @Test
    fun spanishQuestionGetsSpanishAnswer() = runTest {
        val answer = engine.answer(
            question = "¿Cómo funciona la autenticación?",
            intent = AssistantIntentType.QUESTION,
            grounding = listOf(ScoredDocument(authDoc, 0.8)),
        ).getOrNull()

        assertNotNull(answer)
        assertTrue("Esto es lo que dice" in answer.markdown)
        assertTrue("Authentication" in answer.markdown)
    }

    @Test
    fun frenchQuestionGetsFrenchAnswer() = runTest {
        val answer = engine.answer(
            question = "Comment fonctionne l'authentification ?",
            intent = AssistantIntentType.QUESTION,
            grounding = listOf(ScoredDocument(authDoc, 0.8)),
        ).getOrNull()

        assertNotNull(answer)
        assertTrue("Voici ce que" in answer.markdown)
        assertTrue("Authentication" in answer.markdown)
    }

    @Test
    fun spanishNotEnoughInformationIsLocalized() = runTest {
        val answer = engine.answer(
            question = "¿Qué sabes sobre widgets cuánticos?",
            intent = AssistantIntentType.QUESTION,
            grounding = emptyList(),
        ).getOrNull()

        assertNotNull(answer)
        assertEquals(AnswerConfidence.NOT_ENOUGH_INFORMATION, answer.confidence)
        assertTrue("Aun no tengo documentacion" in answer.markdown)
    }

    @Test
    fun frenchNotEnoughInformationIsLocalized() = runTest {
        val answer = engine.answer(
            question = "Parle-moi des widgets quantiques",
            intent = AssistantIntentType.QUESTION,
            grounding = emptyList(),
        ).getOrNull()

        assertNotNull(answer)
        assertEquals(AnswerConfidence.NOT_ENOUGH_INFORMATION, answer.confidence)
        assertTrue("Je n'ai pas encore de documentation" in answer.markdown)
    }

    @Test
    fun summarizeListsKeyPoints() = runTest {
        val answer = engine.answer(
            question = "Summarize the loyalty rewards doc",
            intent = AssistantIntentType.SUMMARIZE,
            grounding = listOf(ScoredDocument(loyaltyDoc, 0.6)),
        ).getOrNull()

        assertNotNull(answer)
        assertTrue("summary" in answer.markdown.lowercase())
        assertTrue("Key points:" in answer.markdown)
    }
}
