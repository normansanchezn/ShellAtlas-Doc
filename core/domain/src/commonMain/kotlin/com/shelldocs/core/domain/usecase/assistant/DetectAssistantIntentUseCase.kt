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
            CREATE_MARKERS.any { it in normalized } -> AssistantIntentType.CREATE_DOCUMENT
            IMPROVE_MARKERS.any { it in normalized } -> AssistantIntentType.IMPROVE_DOCUMENT
            SUMMARY_MARKERS.any { it in normalized } -> AssistantIntentType.SUMMARIZE
            FLOW_MARKERS.any { it in normalized } -> AssistantIntentType.EXPLAIN_FLOW
            else -> AssistantIntentType.QUESTION
        }
    }

    private companion object {
        val CREATE_MARKERS = listOf(
            "create a document", "create a doc", "write a document", "write up a document",
            "draft a document", "new document", "create new document", "document this",
            "crea un documento", "crear un documento", "redacta un documento",
            "nuevo documento", "documenta esto", "escribe un documento", "genera un documento",
            "crea una página", "crea una pagina",
            "crée un document", "créer un document", "rédige un document", "rédiger un document",
            "nouveau document", "génère un document", "documente ceci",
        )
        val FLOW_MARKERS = listOf(
            "flow", "process", "step by step", "how does", "how do", "walk me through",
            "explain", "flujo", "proceso", "paso a paso", "cómo funciona", "como funciona",
            "explica", "explícame",
            "flux", "processus", "étape par étape", "comment fonctionne", "explique",
            // Guided onboarding / KT requests
            "onboarding", "knowledge transfer", "kt session", "new collaborator", "new hire",
            "nuevo colaborador", "nueva colaboradora", "incorporación", "incorporacion",
            "nouveau collaborateur", "nouvelle recrue", "intégration",
        )
        val IMPROVE_MARKERS = listOf(
            "improve", "rewrite", "should i update", "needs update", "outdated?",
            "mejorar", "mejora", "reescribe", "actualizar el documento", "vale la pena actualizar",
            "améliorer", "réécrire", "faut-il mettre à jour", "à mettre à jour",
        )
        val SUMMARY_MARKERS = listOf(
            "summarize", "summary", "tl;dr", "resumen", "resume ", "resúmeme",
            "résume", "résumé",
        )
    }
}
