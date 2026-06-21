package com.shelldocs.core.domain.usecase.assistant

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.getOrDefault
import com.shelldocs.core.common.result.onSuccess
import com.shelldocs.core.domain.entity.assistant.AssistantAnswer
import com.shelldocs.core.domain.entity.assistant.AssistantIntentType
import com.shelldocs.core.domain.entity.assistant.AssistantLanguage
import com.shelldocs.core.domain.entity.assistant.AssistantMessage
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

    suspend operator fun invoke(
        question: String,
        conversationMessages: List<AssistantMessage> = emptyList(),
        language: AssistantLanguage? = null,
    ): DomainResult<AssistantAnswer> {
        val intent = detectIntent(question)
        if (intent == AssistantIntentType.CREATE_DOCUMENT) {
            return createDocumentFromAssistant(roleProvider(), question, language)
        }

        val contextualQuestion = resolveConversationContext(question, conversationMessages)
        val keywords = contextualQuestion.lowercase()
            .split(' ')
            .filter { it.length >= 4 }
            .distinct()
        val questionHash = contextualQuestion.trim().lowercase().hashCode().toString()

        cache.lookup(questionHash, keywords)?.let { cached ->
            return DomainResult.success(cached.copy(fromCache = true))
        }

        val grounding = retrieveGroundingDocuments(contextualQuestion).getOrDefault(emptyList())

        return engine.answer(contextualQuestion, intent, grounding, language).onSuccess { answer ->
            cache.save(questionHash, keywords, answer)
        }
    }

    private fun resolveConversationContext(question: String, conversationMessages: List<AssistantMessage>): String {
        val normalizedQuestion = question.lowercase()
        if (conversationMessages.isEmpty()) return question
        if (TOPIC_SHIFT_MARKERS.any { it in normalizedQuestion }) return question

        val recentMessages = conversationMessages
            .asReversed()
            .filterNot { it.isWelcome }
            .take(MAX_CONTEXT_MESSAGES)
            .asReversed()
        if (recentMessages.isEmpty()) return question

        val shouldUseContext = CONTEXT_CONTINUATION_MARKERS.any { it in normalizedQuestion } ||
                isFollowUpQuestion(normalizedQuestion) ||
                sharesContextTerms(question, recentMessages)
        if (!shouldUseContext) return question

        return buildString {
            appendLine("Conversation context:")
            recentMessages.forEach { message ->
                appendLine("- ${message.role.name.lowercase()}: ${message.markdown}")
            }
            appendLine()
            appendLine("Current question:")
            append(question)
        }.trim()
    }

    private fun isFollowUpQuestion(normalizedQuestion: String): Boolean =
        normalizedQuestion.split(' ').count { it.isNotBlank() } <= 4

    private fun sharesContextTerms(question: String, recentMessages: List<AssistantMessage>): Boolean {
        val questionTerms = tokenize(question)
        if (questionTerms.isEmpty()) return false
        val contextTerms = recentMessages.flatMap { tokenize(it.markdown) }.distinct()
        return questionTerms.any { it in contextTerms }
    }

    private fun tokenize(text: String): List<String> =
        text.lowercase()
            .split(TERM_SPLIT_REGEX)
            .filter { it.length >= MIN_TERM_LENGTH }
            .distinct()

    private companion object {
        const val MAX_CONTEXT_MESSAGES = 6
        const val MIN_TERM_LENGTH = 3
        val TERM_SPLIT_REGEX = Regex("[^a-z0-9áéíóúñü+./-]+")
        val CONTEXT_CONTINUATION_MARKERS = listOf(
            "continue", "go on", "what next", "next step", "and then", "more detail", "more details",
            "keep going", "continue the kt", "sigue", "continua", "continúa", "qué sigue", "que sigue",
            "siguiente", "y luego", "más detalle", "mas detalle", "what about that", "how about that",
        )
        val TOPIC_SHIFT_MARKERS = listOf(
            "new topic", "change topic", "switch topic", "different topic", "another topic",
            "cambia de tema", "otro tema", "cambiar de tema", "otra cosa", "por cierto",
        )
    }
}
