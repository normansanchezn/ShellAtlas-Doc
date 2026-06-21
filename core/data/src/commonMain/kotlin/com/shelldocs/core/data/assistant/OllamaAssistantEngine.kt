package com.shelldocs.core.data.assistant

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.assistant.*
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
        val allPartial = grounding.isNotEmpty() && grounding.all { it.isPartialMatch }
        val confidence = when {
            grounding.isEmpty() -> AnswerConfidence.LOW
            allPartial -> AnswerConfidence.LOW
            else -> AnswerConfidence.fromRetrievalScore(grounding.first().score)
        }
        DomainResult.success(
            AssistantAnswer(
                markdown = markdown.trim(),
                confidence = confidence,
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
        appendLine("You are a Synthesis & Contextual Reasoning Engine, not a simple search tool.")
        appendLine()
        appendLine("CORE BEHAVIOR:")
        appendLine("- Synthesize information across ALL provided documents, not just the top match.")
        appendLine("- When documents contain complementary information, combine them into a coherent narrative.")
        appendLine("- When information is incomplete, explicitly state what was found, what is missing, and suggest concrete next steps.")
        appendLine("- Never give a flat 'not enough information' response. Always provide the best available context.")
        appendLine("- Identify connections between documents: shared concepts, dependencies, and cross-references.")
        appendLine()
        if (language != null) {
            appendLine("Reply in ${language.promptName} — this is the language the rest of the conversation has used.")
        } else {
            appendLine(
                "Reply in the same language the user wrote the question in (Spanish, French or English); " +
                    "default to English if unsure.",
            )
        }
        appendLine()
        when (intent) {
            AssistantIntentType.EXPLAIN_FLOW -> {
                appendLine("The user wants a step-by-step explanation of a flow or process; structure the answer as ordered steps, pulling from multiple documents if they cover different phases.")
                grounding.firstOrNull()?.document?.let { topDocument ->
                    appendLine(AssistantMermaidBuilder.promptHint(question, topDocument))
                }
            }
            AssistantIntentType.IMPROVE_DOCUMENT ->
                appendLine("The user asks whether a document should be improved; judge it honestly and say no when it is healthy.")
            AssistantIntentType.SUMMARIZE ->
                appendLine("The user wants a concise summary with key points. If multiple documents are relevant, synthesize them into a unified summary.")
            AssistantIntentType.CREATE_DOCUMENT ->
                appendLine("The user wants a new document created; explain that you'll set up a draft.")
            AssistantIntentType.QUESTION -> Unit
        }
        appendLine()
        if (grounding.isEmpty()) {
            appendLine("No documentation excerpts matched this query.")
            appendLine("Acknowledge the gap, suggest what kind of documentation would help, and offer to create a draft.")
        } else {
            grounding.forEachIndexed { index, scored ->
                appendLine("--- Document ${index + 1}: ${scored.document.title} (relevance: ${scored.relevancePercent}%) ---")
                appendLine(scored.document.plainText.take(MAX_EXCERPT_CHARS))
                appendLine()
            }
        }
        appendLine("Question: $question")
    }

    private companion object {
        const val MAX_EXCERPT_CHARS = 2400
    }
}
