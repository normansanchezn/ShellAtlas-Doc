package com.shelldocs.core.domain.usecase.assistant

import com.shelldocs.core.domain.entity.assistant.AssistantLanguage

/**
 * Greeting shown when the user opens the assistant with an empty
 * conversation. Sets expectations for what it can do — including acting on
 * requests, not just answering questions — instead of dropping the user on
 * an empty chat.
 */
class BuildWelcomeMessageUseCase {

    operator fun invoke(language: AssistantLanguage = AssistantLanguage.SPANISH): String = when (language) {
        AssistantLanguage.SPANISH -> SPANISH
        AssistantLanguage.FRENCH -> FRENCH
        AssistantLanguage.ENGLISH -> ENGLISH
    }

    private companion object {
        val ENGLISH = """
            Hi, I'm the ShellDoc assistant. I can help you:

            - Open or show you a specific document
            - Find where to look for documentation on a topic
            - Share analytics about the state of the documentation
            - Walk you through a guided KT (Knowledge Transfer) if you're new to the team

            I can also create a new draft document if you ask me to. What would you like to do?
        """.trimIndent()

        val SPANISH = """
            Hola, soy el asistente de ShellDoc. Puedo ayudarte a:

            - Mostrarte un documento o archivo especifico
            - Decirte donde encontrar documentacion sobre un tema
            - Darte analiticas sobre el estado de la documentacion
            - Guiarte en un proceso tipo KT (Knowledge Transfer) si eres nuevo en el equipo

            Tambien puedo crear un borrador de documento nuevo si me lo pides. ¿En que te ayudo hoy?
        """.trimIndent()

        val FRENCH = """
            Bonjour, je suis l'assistant ShellDoc. Je peux t'aider a :

            - T'afficher un document ou fichier specifique
            - T'indiquer ou trouver la documentation sur un sujet
            - Te donner des analyses sur l'etat de la documentation
            - Te guider dans un processus de KT (transfert de connaissances) si tu es nouveau dans l'equipe

            Je peux aussi creer un nouveau brouillon de document si tu me le demandes. Comment puis-je t'aider aujourd'hui ?
        """.trimIndent()
    }
}
