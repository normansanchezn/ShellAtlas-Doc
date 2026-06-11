package com.shelldocs.core.data.assistant

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.getOrNull
import com.shelldocs.core.domain.entity.assistant.AnswerConfidence
import com.shelldocs.core.domain.entity.assistant.AssistantAnswer
import com.shelldocs.core.domain.entity.assistant.AssistantAvailability
import com.shelldocs.core.domain.entity.assistant.AssistantIntentType
import com.shelldocs.core.domain.entity.assistant.AssistantLanguage
import com.shelldocs.core.domain.entity.assistant.ScoredDocument
import com.shelldocs.core.domain.repository.AssistantEngine
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private class StubEngine(
    private val result: DomainResult<AssistantAnswer>,
    private val reachable: Boolean,
    private val label: String,
) : AssistantEngine {
    override suspend fun answer(
        question: String,
        intent: AssistantIntentType,
        grounding: List<ScoredDocument>,
        language: AssistantLanguage?,
    ): DomainResult<AssistantAnswer> = result

    override suspend fun availability() = AssistantAvailability(reachable, label, label)
}

class CompositeAssistantEngineTest {

    private val successAnswer = AssistantAnswer(
        markdown = "primary",
        confidence = AnswerConfidence.HIGH,
        sources = emptyList(),
        intent = AssistantIntentType.QUESTION,
    )
    private val fallbackAnswer = successAnswer.copy(markdown = "fallback")

    @Test
    fun usesPrimaryWhenItSucceeds() = runTest {
        val composite = CompositeAssistantEngine(
            primary = StubEngine(DomainResult.success(successAnswer), reachable = true, label = "llm"),
            fallback = StubEngine(DomainResult.success(fallbackAnswer), reachable = false, label = "grounded"),
        )

        val answer = composite.answer("q", AssistantIntentType.QUESTION, emptyList()).getOrNull()
        assertNotNull(answer)
        assertEquals("primary", answer.markdown)
    }

    @Test
    fun fallsBackWhenPrimaryFails() = runTest {
        val composite = CompositeAssistantEngine(
            primary = StubEngine(DomainResult.failure(AppError.Network()), reachable = false, label = "llm"),
            fallback = StubEngine(DomainResult.success(fallbackAnswer), reachable = false, label = "grounded"),
        )

        val answer = composite.answer("q", AssistantIntentType.QUESTION, emptyList()).getOrNull()
        assertNotNull(answer)
        assertEquals("fallback", answer.markdown)
    }

    @Test
    fun availabilityPrefersReachablePrimary() = runTest {
        val composite = CompositeAssistantEngine(
            primary = StubEngine(DomainResult.success(successAnswer), reachable = true, label = "llm"),
            fallback = StubEngine(DomainResult.success(fallbackAnswer), reachable = false, label = "grounded"),
        )

        assertEquals("llm", composite.availability().modelName)
    }

    @Test
    fun availabilityReportsFallbackWhenPrimaryUnreachable() = runTest {
        val composite = CompositeAssistantEngine(
            primary = StubEngine(DomainResult.failure(AppError.Network()), reachable = false, label = "llm"),
            fallback = StubEngine(DomainResult.success(fallbackAnswer), reachable = false, label = "grounded"),
        )

        assertEquals("grounded", composite.availability().statusMessage)
    }
}
