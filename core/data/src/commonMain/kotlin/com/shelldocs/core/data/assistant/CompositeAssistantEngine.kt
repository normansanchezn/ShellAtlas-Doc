package com.shelldocs.core.data.assistant

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.assistant.AnswerConfidence
import com.shelldocs.core.domain.entity.assistant.AssistantAnswer
import com.shelldocs.core.domain.entity.assistant.AssistantAvailability
import com.shelldocs.core.domain.entity.assistant.AssistantIntentType
import com.shelldocs.core.domain.entity.assistant.AssistantLanguage
import com.shelldocs.core.domain.entity.assistant.ScoredDocument
import com.shelldocs.core.domain.repository.AssistantEngine

/**
 * Tries the LLM-backed engine first. When it's unreachable, this reports a
 * clear "AI unavailable" placeholder instead of silently swapping in the
 * grounded engine's raw multi-document synthesis — that synthesis just
 * concatenates document excerpts verbatim (including in whatever language
 * each source document happens to be written in), which reads as a broken,
 * language-mixed answer rather than a real one when surfaced as if the AI
 * had produced it.
 */
class CompositeAssistantEngine(
    private val primary: AssistantEngine,
    private val fallback: AssistantEngine,
) : AssistantEngine {

    override suspend fun answer(
        question: String,
        intent: AssistantIntentType,
        grounding: List<ScoredDocument>,
        language: AssistantLanguage?,
    ): DomainResult<AssistantAnswer> =
        when (val primaryResult = primary.answer(question, intent, grounding, language)) {
            is DomainResult.Success -> primaryResult
            is DomainResult.Failure -> DomainResult.success(unavailableAnswer(intent, language))
        }

    override suspend fun availability(): AssistantAvailability {
        val primaryAvailability = primary.availability()
        return if (primaryAvailability.isLlmReachable) primaryAvailability else fallback.availability()
    }

    private fun unavailableAnswer(intent: AssistantIntentType, language: AssistantLanguage?): AssistantAnswer =
        AssistantAnswer(
            markdown = UnavailableCopy.of(language ?: AssistantLanguage.ENGLISH),
            confidence = AnswerConfidence.NOT_ENOUGH_INFORMATION,
            sources = emptyList(),
            intent = intent,
            isUnavailable = true,
        )
}

private object UnavailableCopy {
    fun of(language: AssistantLanguage): String = when (language) {
        AssistantLanguage.SPANISH ->
            "El asistente de IA no está disponible en este momento. Podés explorar la documentación indexada directamente mientras se restablece el servicio."
        AssistantLanguage.FRENCH ->
            "L'assistant IA n'est pas disponible pour le moment. Vous pouvez consulter la documentation indexée directement en attendant que le service soit rétabli."
        AssistantLanguage.ENGLISH ->
            "The AI assistant is unavailable right now. You can browse the indexed documentation directly while the service comes back."
    }
}
