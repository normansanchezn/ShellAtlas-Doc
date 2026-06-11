package com.shelldocs.core.domain.entity.assistant

import kotlin.test.Test
import kotlin.test.assertEquals

class AnswerConfidenceTest {

    @Test
    fun retrievalScoreMapsToConfidenceTiers() {
        assertEquals(AnswerConfidence.HIGH, AnswerConfidence.fromRetrievalScore(0.9))
        assertEquals(AnswerConfidence.HIGH, AnswerConfidence.fromRetrievalScore(0.62))
        assertEquals(AnswerConfidence.MEDIUM, AnswerConfidence.fromRetrievalScore(0.5))
        assertEquals(AnswerConfidence.LOW, AnswerConfidence.fromRetrievalScore(0.1))
        assertEquals(AnswerConfidence.NOT_ENOUGH_INFORMATION, AnswerConfidence.fromRetrievalScore(0.0))
    }
}
