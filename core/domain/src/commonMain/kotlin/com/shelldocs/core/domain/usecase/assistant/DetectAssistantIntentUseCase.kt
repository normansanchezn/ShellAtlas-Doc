package com.shelldocs.core.domain.usecase.assistant

import com.shelldocs.core.domain.entity.assistant.AssistantIntentType

/**
 * Lightweight bilingual (EN/ES) intent classifier so the assistant knows
 * whether to explain a flow, audit a document or answer a plain question.
 */
class DetectAssistantIntentUseCase {

    operator fun invoke(question: String): AssistantIntentType {
        val normalized = question.lowercase()
        return when {
            IMPROVE_MARKERS.any { it in normalized } -> AssistantIntentType.IMPROVE_DOCUMENT
            SUMMARY_MARKERS.any { it in normalized } -> AssistantIntentType.SUMMARIZE
            FLOW_MARKERS.any { it in normalized } -> AssistantIntentType.EXPLAIN_FLOW
            else -> AssistantIntentType.QUESTION
        }
    }

    private companion object {
        val FLOW_MARKERS = listOf(
            "flow", "process", "step by step", "how does", "how do", "walk me through",
            "explain", "flujo", "proceso", "paso a paso", "cómo funciona", "como funciona",
            "explica", "explícame",
        )
        val IMPROVE_MARKERS = listOf(
            "improve", "rewrite", "should i update", "needs update", "outdated?",
            "mejorar", "mejora", "reescribe", "actualizar el documento", "vale la pena actualizar",
        )
        val SUMMARY_MARKERS = listOf(
            "summarize", "summary", "tl;dr", "resumen", "resume ", "resúmeme",
        )
    }
}
