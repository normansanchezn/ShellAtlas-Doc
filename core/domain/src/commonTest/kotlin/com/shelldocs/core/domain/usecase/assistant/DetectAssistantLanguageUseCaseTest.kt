package com.shelldocs.core.domain.usecase.assistant

import com.shelldocs.core.domain.entity.assistant.AssistantLanguage
import kotlin.test.Test
import kotlin.test.assertEquals

class DetectAssistantLanguageUseCaseTest {

    private val detect = DetectAssistantLanguageUseCase()

    @Test
    fun detectsSpanishFromAccentsAndFunctionWords() {
        assertEquals(AssistantLanguage.SPANISH, detect("¿Cómo funciona la autenticación?"))
    }

    @Test
    fun detectsFrenchFromAccentsAndFunctionWords() {
        assertEquals(AssistantLanguage.FRENCH, detect("Comment fonctionne l'authentification ?"))
    }

    @Test
    fun detectsEnglishFromFunctionWords() {
        assertEquals(AssistantLanguage.ENGLISH, detect("What happens when tokens expire?"))
    }

    @Test
    fun fallsBackToDefaultWhenNoHintsMatch() {
        assertEquals(AssistantLanguage.ENGLISH, detect("quantum widgets"))
        assertEquals(AssistantLanguage.SPANISH, detect("quantum widgets", default = AssistantLanguage.SPANISH))
    }
}
