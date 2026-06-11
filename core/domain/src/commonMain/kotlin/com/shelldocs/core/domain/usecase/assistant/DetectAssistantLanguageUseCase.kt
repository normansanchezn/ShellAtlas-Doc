package com.shelldocs.core.domain.usecase.assistant

import com.shelldocs.core.domain.entity.assistant.AssistantLanguage

/**
 * Lightweight EN/ES/FR language detector based on diacritics and common
 * function words, so the assistant can reply in the language the user
 * actually wrote in.
 */
class DetectAssistantLanguageUseCase {

    operator fun invoke(text: String, default: AssistantLanguage = AssistantLanguage.ENGLISH): AssistantLanguage {
        val normalized = " ${text.lowercase()} "
        val scores = mapOf(
            AssistantLanguage.SPANISH to SPANISH_HINTS.count { it in normalized },
            AssistantLanguage.FRENCH to FRENCH_HINTS.count { it in normalized },
            AssistantLanguage.ENGLISH to ENGLISH_HINTS.count { it in normalized },
        )
        val maxScore = scores.values.max()
        if (maxScore == 0) return default
        val winners = scores.filterValues { it == maxScore }.keys
        return if (default in winners) default else winners.first()
    }

    private companion object {
        val SPANISH_HINTS = listOf(
            "á", "é", "í", "ó", "ú", "ñ", "¿", "¡",
            " qué ", " que ", " cómo ", " como ", " cuál ", " cual ", " dónde ", " donde ",
            " está ", " esta ", " para ", " sobre ", " necesito ", " quiero ", " documento ",
            " crea ", " crear ", " nuevo ", " nueva ", " esto ", " gracias ", " por favor ",
        )
        val FRENCH_HINTS = listOf(
            "à", "â", "ç", "è", "ê", "ë", "î", "ï", "ô", "ù", "û", "ü", "œ",
            " le ", " la ", " les ", " un ", " une ", " des ", " est ", " sont ",
            " comment ", " pourquoi ", " où ", " quel ", " quelle ", " avec ", " pour ",
            " document ", " nouveau ", " nouvelle ", " merci ", " s'il vous plaît ", " bonjour ",
            " crée ", " créer ", " génère ",
        )
        val ENGLISH_HINTS = listOf(
            " the ", " what ", " how ", " where ", " which ", " with ", " for ",
            " document ", " new ", " create ", " please ", " thanks ", " is ", " are ",
        )
    }
}
