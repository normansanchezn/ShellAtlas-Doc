package com.shelldocs.core.domain.usecase.onboarding

import com.shelldocs.core.domain.entity.assistant.AssistantLanguage
import com.shelldocs.core.domain.entity.onboarding.KnowledgeCheckpoint
import com.shelldocs.core.domain.entity.onboarding.KnowledgeProgress
import com.shelldocs.core.domain.entity.onboarding.QuizAttempt

/** Builds the assistant messages for the guided Knowledge Transfer flow. */
class BuildKnowledgeTransferMessageUseCase {

    fun step(checkpoint: KnowledgeCheckpoint, progress: KnowledgeProgress, language: AssistantLanguage): String {
        val stepNumber = progress.completed + 1
        val header = when (language) {
            AssistantLanguage.SPANISH -> "Knowledge Transfer — Paso $stepNumber/${progress.total}: ${checkpoint.title}"
            AssistantLanguage.ENGLISH -> "Knowledge Transfer — Step $stepNumber/${progress.total}: ${checkpoint.title}"
            AssistantLanguage.FRENCH -> "Transfert de connaissances — Etape $stepNumber/${progress.total} : ${checkpoint.title}"
        }
        return "**$header**\n\n${checkpoint.instruction}"
    }

    fun completion(progress: KnowledgeProgress, attempts: List<QuizAttempt>, language: AssistantLanguage): String {
        val avgScore = if (attempts.isEmpty()) 0 else attempts.sumOf { it.scorePercent } / attempts.size
        val header = when (language) {
            AssistantLanguage.SPANISH -> "🎓 Knowledge Transfer completo"
            AssistantLanguage.ENGLISH -> "🎓 Knowledge Transfer complete"
            AssistantLanguage.FRENCH -> "🎓 Transfert de connaissances termine"
        }
        val body = when (language) {
            AssistantLanguage.SPANISH ->
                "Completaste los ${progress.total} checkpoints, con ${attempts.size} quiz(zes) respondidos " +
                        "(promedio ${avgScore}%).\n\nTu Project Knowledge Score es ${progress.percent}% — puedes verlo en el Dashboard."

            AssistantLanguage.ENGLISH ->
                "You completed all ${progress.total} checkpoints, answering ${attempts.size} quiz(zes) " +
                        "(average ${avgScore}%).\n\nYour Project Knowledge Score is ${progress.percent}% — check it on the Dashboard."

            AssistantLanguage.FRENCH ->
                "Tu as termine les ${progress.total} etapes, avec ${attempts.size} quiz repondu(s) " +
                        "(moyenne ${avgScore}%).\n\nTon score de connaissance du projet est de ${progress.percent}% — visible sur le Dashboard."
        }
        return "**$header**\n\n$body"
    }
}
