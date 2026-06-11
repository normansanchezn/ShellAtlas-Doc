package com.shelldocs.core.data.assistant

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.assistant.AnswerConfidence
import com.shelldocs.core.domain.entity.assistant.AnswerSource
import com.shelldocs.core.domain.entity.assistant.AssistantAnswer
import com.shelldocs.core.domain.entity.assistant.AssistantAvailability
import com.shelldocs.core.domain.entity.assistant.AssistantIntentType
import com.shelldocs.core.domain.entity.assistant.AssistantLanguage
import com.shelldocs.core.domain.entity.assistant.ScoredDocument
import com.shelldocs.core.domain.repository.AssistantEngine

/**
 * LLM-backed engine. The prompt is strictly grounded: the model only sees
 * retrieved documentation excerpts and is instructed not to invent facts.
 */
class OllamaAssistantEngine(private val client: OllamaClient) : AssistantEngine {

    override suspend fun answer(
        question: String,
        intent: AssistantIntentType,
        grounding: List<ScoredDocument>,
        language: AssistantLanguage?,
    ): DomainResult<AssistantAnswer> = try {
        val markdown = client.generate(buildPrompt(question, intent, grounding, language))
        DomainResult.success(
            AssistantAnswer(
                markdown = markdown.trim(),
                confidence = grounding.firstOrNull()
                    ?.let { AnswerConfidence.fromRetrievalScore(it.score) }
                    ?: AnswerConfidence.NOT_ENOUGH_INFORMATION,
                sources = grounding.map {
                    AnswerSource(
                        documentId = it.document.id,
                        title = it.document.title,
                        breadcrumb = it.document.attributes.module,
                        relevance = it.relevancePercent,
                    )
                },
                intent = intent,
            ),
        )
    } catch (exception: Exception) {
        DomainResult.failure(AppError.Network(exception.message ?: "Ollama unavailable"))
    }

    override suspend fun availability(): AssistantAvailability {
        val reachable = client.isReachable()
        return AssistantAvailability(
            isLlmReachable = reachable,
            modelName = client.modelName.takeIf { reachable },
            statusMessage = if (reachable) "AI available" else "Ollama is not running",
        )
    }

    private fun buildPrompt(
        question: String,
        intent: AssistantIntentType,
        grounding: List<ScoredDocument>,
        language: AssistantLanguage?,
    ): String = buildString {
        appendLine("You are ShellDoc AI, the assistant of an enterprise knowledge platform.")
        appendLine("Answer ONLY with facts from the documentation excerpts below.")
        if (language != null) {
            appendLine("Reply in ${language.promptName} — this is the language the rest of the conversation has used.")
        } else {
            appendLine(
                "Reply in the same language the user wrote the question in (Spanish, French or English); " +
                    "default to English if unsure.",
            )
        }
        appendLine(
            "If the excerpts do not contain the answer, say so naturally in that language, suggest other " +
                "search terms, and offer to create a draft document about the topic instead of a flat error.",
        )
        when (intent) {
            AssistantIntentType.EXPLAIN_FLOW ->
                appendLine("The user wants a step-by-step explanation of a flow or process; structure the answer as ordered steps.")
            AssistantIntentType.IMPROVE_DOCUMENT ->
                appendLine("The user asks whether a document should be improved; judge it honestly and say no when it is healthy.")
            AssistantIntentType.SUMMARIZE ->
                appendLine("The user wants a concise summary with key points.")
            AssistantIntentType.CREATE_DOCUMENT ->
                appendLine("The user wants a new document created; explain that you'll set up a draft.")
            AssistantIntentType.QUESTION -> Unit
        }
        appendLine()
        grounding.forEachIndexed { index, scored ->
            appendLine("--- Document ${index + 1}: ${scored.document.title} ---")
            appendLine(scored.document.plainText.take(MAX_EXCERPT_CHARS))
            appendLine()
        }
        appendLine("Question: $question")
    }

    private companion object {
        const val MAX_EXCERPT_CHARS = 2400
    }
}
