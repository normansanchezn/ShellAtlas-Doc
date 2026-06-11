package com.shelldocs.core.domain.repository

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.assistant.AssistantAnswer
import com.shelldocs.core.domain.entity.assistant.AssistantAvailability
import com.shelldocs.core.domain.entity.assistant.AssistantIntentType
import com.shelldocs.core.domain.entity.document.Document

/**
 * Strategy that turns a question plus retrieved documents into a grounded
 * answer. Implementations: local LLM (Ollama), grounded template engine,
 * or a composite that falls back gracefully.
 */
interface AssistantEngine {

    suspend fun answer(
        question: String,
        intent: AssistantIntentType,
        groundingDocuments: List<Document>,
    ): DomainResult<AssistantAnswer>

    suspend fun availability(): AssistantAvailability
}
