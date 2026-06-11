package com.shelldocs.core.domain.usecase.assistant

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.getOrDefault
import com.shelldocs.core.common.result.onSuccess
import com.shelldocs.core.domain.entity.assistant.AssistantAnswer
import com.shelldocs.core.domain.entity.assistant.AssistantIntentType
import com.shelldocs.core.domain.entity.assistant.AssistantLanguage
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.repository.AssistantCacheRepository
import com.shelldocs.core.domain.repository.AssistantEngine

/**
 * Orchestrates one assistant turn:
 * 1. classify the intent, 2. serve from cache when possible,
 * 3. retrieve grounding documents, 4. delegate to the engine,
 * 5. persist the new answer in the intelligence cache.
 *
 * `CREATE_DOCUMENT` is handled as an action rather than a grounded answer:
 * it bypasses cache and grounding and goes straight to document creation.
 */
class AskAssistantUseCase(
    private val detectIntent: DetectAssistantIntentUseCase,
    private val retrieveGroundingDocuments: RetrieveGroundingDocumentsUseCase,
    private val engine: AssistantEngine,
    private val cache: AssistantCacheRepository,
    private val createDocumentFromAssistant: CreateDocumentFromAssistantUseCase,
    private val roleProvider: () -> UserRole,
) {

    suspend operator fun invoke(question: String, language: AssistantLanguage? = null): DomainResult<AssistantAnswer> {
        val intent = detectIntent(question)
        if (intent == AssistantIntentType.CREATE_DOCUMENT) {
            return createDocumentFromAssistant(roleProvider(), question, language)
        }

        val keywords = question.lowercase()
            .split(' ')
            .filter { it.length >= 4 }
            .distinct()
        val questionHash = question.trim().lowercase().hashCode().toString()

        cache.lookup(questionHash, keywords)?.let { cached ->
            return DomainResult.success(cached.copy(fromCache = true))
        }

        val grounding = retrieveGroundingDocuments(question).getOrDefault(emptyList())

        return engine.answer(question, intent, grounding, language).onSuccess { answer ->
            cache.save(questionHash, keywords, answer)
        }
    }
}
