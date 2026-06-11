package com.shelldocs.core.domain.usecase.assistant

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.getOrDefault
import com.shelldocs.core.common.result.onSuccess
import com.shelldocs.core.domain.entity.assistant.AssistantAnswer
import com.shelldocs.core.domain.repository.AssistantCacheRepository
import com.shelldocs.core.domain.repository.AssistantEngine

/**
 * Orchestrates one assistant turn:
 * 1. classify the intent, 2. serve from cache when possible,
 * 3. retrieve grounding documents, 4. delegate to the engine,
 * 5. persist the new answer in the intelligence cache.
 */
class AskAssistantUseCase(
    private val detectIntent: DetectAssistantIntentUseCase,
    private val retrieveGroundingDocuments: RetrieveGroundingDocumentsUseCase,
    private val engine: AssistantEngine,
    private val cache: AssistantCacheRepository,
) {

    suspend operator fun invoke(question: String): DomainResult<AssistantAnswer> {
        val intent = detectIntent(question)
        val keywords = question.lowercase()
            .split(' ')
            .filter { it.length >= 4 }
            .distinct()
        val questionHash = question.trim().lowercase().hashCode().toString()

        cache.lookup(questionHash, keywords)?.let { cached ->
            return DomainResult.success(cached.copy(fromCache = true))
        }

        val grounding = retrieveGroundingDocuments(question)
            .getOrDefault(emptyList())
            .map { it.document }

        return engine.answer(question, intent, grounding).onSuccess { answer ->
            cache.save(questionHash, keywords, answer)
        }
    }
}
