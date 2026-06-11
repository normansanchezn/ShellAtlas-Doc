package com.shelldocs.core.domain.usecase.assistant

import com.shelldocs.core.domain.entity.assistant.AssistantIntentType
import kotlin.test.Test
import kotlin.test.assertEquals

class DetectAssistantIntentUseCaseTest {

    private val detect = DetectAssistantIntentUseCase()

    @Test
    fun detectsFlowExplanationsInEnglishAndSpanish() {
        assertEquals(AssistantIntentType.EXPLAIN_FLOW, detect("Explain the release process"))
        assertEquals(AssistantIntentType.EXPLAIN_FLOW, detect("How does the token refresh flow work?"))
        assertEquals(AssistantIntentType.EXPLAIN_FLOW, detect("Explícame el flujo de autenticación paso a paso"))
    }

    @Test
    fun detectsImprovementRequests() {
        assertEquals(AssistantIntentType.IMPROVE_DOCUMENT, detect("Should we improve the loyalty doc?"))
        assertEquals(AssistantIntentType.IMPROVE_DOCUMENT, detect("¿Vale la pena actualizar el documento de pagos?"))
    }

    @Test
    fun detectsSummaries() {
        assertEquals(AssistantIntentType.SUMMARIZE, detect("Summarize push notifications doc"))
        assertEquals(AssistantIntentType.SUMMARIZE, detect("Dame un resumen de Station Locator"))
    }

    @Test
    fun fallsBackToPlainQuestion() {
        assertEquals(AssistantIntentType.QUESTION, detect("What is the iOS token expiry?"))
    }

    @Test
    fun improvementWinsOverFlowWhenBothMarkersArePresent() {
        assertEquals(AssistantIntentType.IMPROVE_DOCUMENT, detect("Improve the explanation of the flow"))
    }
}
