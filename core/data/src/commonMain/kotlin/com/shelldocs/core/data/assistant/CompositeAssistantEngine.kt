package com.shelldocs.core.data.assistant

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.assistant.AssistantAnswer
import com.shelldocs.core.domain.entity.assistant.AssistantAvailability
import com.shelldocs.core.domain.entity.assistant.AssistantIntentType
import com.shelldocs.core.domain.entity.assistant.ScoredDocument
import com.shelldocs.core.domain.repository.AssistantEngine

/**
 * Tries the LLM-backed engine first and falls back to the grounded engine,
 * so the assistant always answers — with or without a local model.
 */
class CompositeAssistantEngine(
    private val primary: AssistantEngine,
    private val fallback: AssistantEngine,
) : AssistantEngine {

    override suspend fun answer(
        question: String,
        intent: AssistantIntentType,
        grounding: List<ScoredDocument>,
    ): DomainResult<AssistantAnswer> =
        when (val primaryResult = primary.answer(question, intent, grounding)) {
            is DomainResult.Success -> primaryResult
            is DomainResult.Failure -> fallback.answer(question, intent, grounding)
        }

    override suspend fun availability(): AssistantAvailability {
        val primaryAvailability = primary.availability()
        return if (primaryAvailability.isLlmReachable) primaryAvailability else fallback.availability()
    }
}
