package com.shelldocs.core.domain.usecase.onboarding

/** Detects whether a user message acknowledges finishing a Knowledge Transfer step. */
class DetectCheckpointCompletionUseCase {

    operator fun invoke(message: String): Boolean {
        val normalized = message.trim().lowercase()
        return DONE_PHRASES.any { normalized.contains(it) }
    }

    private companion object {
        val DONE_PHRASES = listOf(
            // Spanish
            "listo", "hecho", "termine", "terminé", "ya revis", "completad", "ya esta", "ya está",
            // English
            "done", "finished", "ready", "got it", "complete",
            // French
            "termine", "terminé", "fini", "pret", "prêt",
        )
    }
}
