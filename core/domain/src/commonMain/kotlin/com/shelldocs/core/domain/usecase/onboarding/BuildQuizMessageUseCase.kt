package com.shelldocs.core.domain.usecase.onboarding

import com.shelldocs.core.domain.entity.assistant.AssistantLanguage
import com.shelldocs.core.domain.entity.onboarding.QuizAttempt
import com.shelldocs.core.domain.entity.onboarding.QuizQuestion

/** Renders the quiz prompt and the pass/retry feedback for the guided Knowledge Transfer flow. */
class BuildQuizMessageUseCase {

    fun prompt(questions: List<QuizQuestion>, language: AssistantLanguage): String {
        val header = when (language) {
            AssistantLanguage.SPANISH -> "Antes de avanzar, responde este quiz rápido."
            AssistantLanguage.ENGLISH -> "Before advancing, answer this quick quiz."
            AssistantLanguage.FRENCH -> "Avant d'avancer, reponds a ce petit quiz."
        }
        val instructions = when (language) {
            AssistantLanguage.SPANISH -> "Responde así: 1a 2c (necesitas 80% para avanzar)."
            AssistantLanguage.ENGLISH -> "Reply like this: 1a 2c (you need 80% to advance)."
            AssistantLanguage.FRENCH -> "Reponds ainsi : 1a 2c (80% requis pour avancer)."
        }
        val body = questions.mapIndexed { index, question ->
            val options = question.options.mapIndexed { optionIndex, option ->
                "   ${('a' + optionIndex)}) $option"
            }.joinToString("\n")
            "${index + 1}. ${question.prompt}\n$options"
        }.joinToString("\n\n")
        return "**$header**\n\n$body\n\n$instructions"
    }

    fun feedback(attempt: QuizAttempt, language: AssistantLanguage): String = when {
        attempt.passed -> when (language) {
            AssistantLanguage.SPANISH -> "✅ ${attempt.scorePercent}% — ¡aprobado! Avanzamos al siguiente paso."
            AssistantLanguage.ENGLISH -> "✅ ${attempt.scorePercent}% — passed! Moving to the next step."
            AssistantLanguage.FRENCH -> "✅ ${attempt.scorePercent}% — reussi ! On passe a l'etape suivante."
        }

        else -> when (language) {
            AssistantLanguage.SPANISH ->
                "❌ ${attempt.scorePercent}% — necesitas ${QuizAttempt.PASSING_SCORE}% para avanzar. " +
                        "Revisa el documento de este paso e intenta de nuevo: responde 1a 2c."

            AssistantLanguage.ENGLISH ->
                "❌ ${attempt.scorePercent}% — you need ${QuizAttempt.PASSING_SCORE}% to advance. " +
                        "Review this step's document and try again: reply 1a 2c."

            AssistantLanguage.FRENCH ->
                "❌ ${attempt.scorePercent}% — ${QuizAttempt.PASSING_SCORE}% requis pour avancer. " +
                        "Relis le document de cette etape et reessaie : reponds 1a 2c."
        }
    }
}
