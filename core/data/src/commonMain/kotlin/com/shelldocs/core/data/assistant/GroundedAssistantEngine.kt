package com.shelldocs.core.data.assistant

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.data.markdown.MarkdownParser
import com.shelldocs.core.domain.entity.assistant.AnswerConfidence
import com.shelldocs.core.domain.entity.assistant.AnswerSource
import com.shelldocs.core.domain.entity.assistant.AssistantAnswer
import com.shelldocs.core.domain.entity.assistant.AssistantAvailability
import com.shelldocs.core.domain.entity.assistant.AssistantIntentType
import com.shelldocs.core.domain.entity.assistant.ScoredDocument
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.HeadingBlock
import com.shelldocs.core.domain.entity.document.ListBlock
import com.shelldocs.core.domain.entity.document.ParagraphBlock
import com.shelldocs.core.domain.repository.AssistantEngine
import com.shelldocs.core.domain.usecase.assistant.ShouldImproveDocumentUseCase

/**
 * Deterministic, fully offline assistant. Answers are always grounded on
 * indexed documents:
 *
 * - QUESTION: quotes the most relevant passages of the top document.
 * - EXPLAIN_FLOW: rebuilds the document structure as a step-by-step walkthrough.
 * - IMPROVE_DOCUMENT: audits health and explicitly refuses to rewrite
 *   healthy documents, explaining why.
 * - SUMMARIZE: condenses the top document into its key points.
 */
class GroundedAssistantEngine(
    private val shouldImproveDocument: ShouldImproveDocumentUseCase,
    private val markdownParser: MarkdownParser = MarkdownParser(),
) : AssistantEngine {

    override suspend fun answer(
        question: String,
        intent: AssistantIntentType,
        grounding: List<ScoredDocument>,
    ): DomainResult<AssistantAnswer> {
        if (grounding.isEmpty()) {
            return DomainResult.success(notEnoughInformation(intent))
        }
        val top = grounding.first()
        val markdown = when (intent) {
            AssistantIntentType.QUESTION -> answerQuestion(question, top.document)
            AssistantIntentType.EXPLAIN_FLOW -> explainFlow(top.document)
            AssistantIntentType.IMPROVE_DOCUMENT -> adviseOnImprovement(top.document)
            AssistantIntentType.SUMMARIZE -> summarize(top.document)
        }
        return DomainResult.success(
            AssistantAnswer(
                markdown = markdown,
                confidence = AnswerConfidence.fromRetrievalScore(top.score),
                sources = grounding.map { scored ->
                    AnswerSource(
                        documentId = scored.document.id,
                        title = scored.document.title,
                        breadcrumb = breadcrumb(scored.document),
                        relevance = scored.relevancePercent,
                    )
                },
                intent = intent,
            ),
        )
    }

    override suspend fun availability(): AssistantAvailability = AssistantAvailability(
        isLlmReachable = false,
        modelName = null,
        statusMessage = "Grounded engine — answers come strictly from indexed documentation",
    )

    private fun answerQuestion(question: String, document: Document): String {
        val blocks = blocksOf(document)
        val terms = question.lowercase().split(' ').filter { it.length >= 4 }
        val relevantParagraphs = blocks
            .filterIsInstance<ParagraphBlock>()
            .filter { paragraph -> terms.any { it in paragraph.text.lowercase() } }
            .take(2)
        return buildString {
            appendLine("Here's what **${document.title}** says about that:")
            appendLine()
            if (relevantParagraphs.isEmpty()) {
                appendLine(document.summary.ifBlank { firstParagraph(blocks) })
            } else {
                relevantParagraphs.forEach { appendLine(it.text); appendLine() }
            }
            append("If you need the full context, open the source below.")
        }.trim()
    }

    private fun explainFlow(document: Document): String {
        val blocks = blocksOf(document)
        val sections = mutableListOf<Pair<String, List<String>>>()
        var currentHeading: String? = null
        var currentSteps = mutableListOf<String>()
        blocks.forEach { block ->
            when (block) {
                is HeadingBlock -> if (block.level > 1) {
                    currentHeading?.let { sections += it to currentSteps.toList() }
                    currentHeading = block.text
                    currentSteps = mutableListOf()
                }
                is ListBlock -> currentSteps += block.items
                is ParagraphBlock -> if (currentHeading != null && currentSteps.isEmpty()) {
                    currentSteps += block.text
                }
                else -> Unit
            }
        }
        currentHeading?.let { sections += it to currentSteps.toList() }

        return buildString {
            appendLine("Here's how **${document.title}** works, step by step:")
            appendLine()
            if (sections.isEmpty()) {
                appendLine(document.summary.ifBlank { firstParagraph(blocks) })
            } else {
                sections.forEachIndexed { index, (heading, steps) ->
                    appendLine("${index + 1}. **$heading**")
                    steps.take(MAX_STEPS_PER_SECTION).forEach { appendLine("   - $it") }
                }
            }
            appendLine()
            append("Each stage is documented in detail in the cited source.")
        }.trim()
    }

    private fun adviseOnImprovement(document: Document): String {
        val decision = shouldImproveDocument(document)
        return if (!decision.shouldImprove) {
            buildString {
                appendLine("I reviewed **${document.title}** and I would *not* rewrite it right now.")
                appendLine()
                appendLine("Health score: **${decision.healthScore}/100**.")
                decision.reasons.forEach { appendLine("- $it") }
            }.trim()
        } else {
            buildString {
                appendLine("**${document.title}** does need an update — health score **${decision.healthScore}/100**.")
                appendLine()
                appendLine("What's wrong:")
                decision.reasons.forEach { appendLine("- $it") }
                appendLine()
                appendLine("Suggested improvements:")
                decision.suggestions.forEach { appendLine("- $it") }
            }.trim()
        }
    }

    private fun summarize(document: Document): String {
        val blocks = blocksOf(document)
        val keyPoints = blocks.filterIsInstance<ListBlock>().flatMap { it.items }.take(MAX_SUMMARY_POINTS)
        return buildString {
            appendLine("**${document.title}** — summary:")
            appendLine()
            appendLine(document.summary.ifBlank { firstParagraph(blocks) })
            if (keyPoints.isNotEmpty()) {
                appendLine()
                appendLine("Key points:")
                keyPoints.forEach { appendLine("- $it") }
            }
        }.trim()
    }

    private fun notEnoughInformation(intent: AssistantIntentType) = AssistantAnswer(
        markdown = "I couldn't find indexed documentation that covers this. " +
            "Try rephrasing with module or feature names (for example *authentication*, " +
            "*loyalty rewards*, *release process*), or import the relevant source first.",
        confidence = AnswerConfidence.NOT_ENOUGH_INFORMATION,
        sources = emptyList(),
        intent = intent,
    )

    private fun blocksOf(document: Document) =
        document.content.blocks.ifEmpty { markdownParser.parse(document.rawMarkdown).content.blocks }

    private fun firstParagraph(blocks: List<com.shelldocs.core.domain.entity.document.ContentBlock>): String =
        blocks.filterIsInstance<ParagraphBlock>().firstOrNull()?.text
            ?: "The document has no narrative content yet."

    private fun breadcrumb(document: Document): String = listOf(
        document.attributes.team.ifBlank { document.attributes.platform },
        document.attributes.module,
    ).filter { it.isNotBlank() }.joinToString(" / ")

    private companion object {
        const val MAX_STEPS_PER_SECTION = 4
        const val MAX_SUMMARY_POINTS = 5
    }
}
