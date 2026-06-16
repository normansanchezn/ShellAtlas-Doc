package com.shelldocs.core.domain.usecase.assistant

import com.shelldocs.core.domain.entity.assistant.AssistantLanguage
import kotlin.test.Test
import kotlin.test.assertTrue

class BuildWelcomeMessageUseCaseTest {

    private val buildWelcomeMessage = BuildWelcomeMessageUseCase()

    @Test
    fun englishWelcomeMentionsAllCapabilities() {
        val message = buildWelcomeMessage(AssistantLanguage.ENGLISH)
        assertTrue(message.contains("Open or show you a specific document"))
        assertTrue(message.contains("Walk you through a guided KT"))
        assertTrue(message.contains("create a new draft document"))
    }

    @Test
    fun spanishWelcomeMentionsAllCapabilities() {
        val message = buildWelcomeMessage(AssistantLanguage.SPANISH)
        assertTrue(message.contains("Mostrarte un documento o archivo especifico"))
        assertTrue(message.contains("proceso tipo KT"))
        assertTrue(message.contains("crear un borrador de documento nuevo"))
    }

    @Test
    fun frenchWelcomeMentionsAllCapabilities() {
        val message = buildWelcomeMessage(AssistantLanguage.FRENCH)
        assertTrue(message.contains("T'afficher un document ou fichier specifique"))
        assertTrue(message.contains("processus de KT"))
        assertTrue(message.contains("creer un nouveau brouillon"))
    }

    @Test
    fun defaultsToSpanish() {
        assertTrue(buildWelcomeMessage().contains("Hola, soy el asistente de ShellAtlas"))
    }
}
