package com.shelldocs.core.domain.usecase.onboarding

import com.shelldocs.core.domain.entity.assistant.AssistantLanguage
import com.shelldocs.core.domain.entity.onboarding.KnowledgeCheckpoint
import com.shelldocs.core.domain.entity.onboarding.KnowledgeProgress

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

    fun completion(progress: KnowledgeProgress, language: AssistantLanguage): String = when (language) {
        AssistantLanguage.SPANISH ->
            "Has completado los ${progress.total} checkpoints del Knowledge Transfer. " +
                "Tu Project Knowledge Score es ${progress.percent}% — puedes verlo en el Dashboard."
        AssistantLanguage.ENGLISH ->
            "You've completed all ${progress.total} Knowledge Transfer checkpoints. " +
                "Your Project Knowledge Score is ${progress.percent}% — check it on the Dashboard."
        AssistantLanguage.FRENCH ->
            "Tu as termine les ${progress.total} etapes du Knowledge Transfer. " +
                "Ton score de connaissance du projet est de ${progress.percent}% — visible sur le Dashboard."
    }
}
