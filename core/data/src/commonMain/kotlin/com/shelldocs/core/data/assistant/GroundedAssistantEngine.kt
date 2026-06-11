package com.shelldocs.core.data.assistant

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.data.markdown.MarkdownParser
import com.shelldocs.core.domain.entity.assistant.AnswerConfidence
import com.shelldocs.core.domain.entity.assistant.AnswerSource
import com.shelldocs.core.domain.entity.assistant.AssistantAnswer
import com.shelldocs.core.domain.entity.assistant.AssistantAvailability
import com.shelldocs.core.domain.entity.assistant.AssistantIntentType
import com.shelldocs.core.domain.entity.assistant.AssistantLanguage
import com.shelldocs.core.domain.entity.assistant.ScoredDocument
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.HeadingBlock
import com.shelldocs.core.domain.entity.document.ListBlock
import com.shelldocs.core.domain.entity.document.ParagraphBlock
import com.shelldocs.core.domain.repository.AssistantEngine
import com.shelldocs.core.domain.usecase.assistant.DetectAssistantLanguageUseCase
import com.shelldocs.core.domain.usecase.assistant.ShouldImproveDocumentUseCase

/**
 * Deterministic, fully offline assistant. Answers are always grounded on
 * indexed documents and reply in the language the user wrote in (EN/ES/FR):
 *
 * - QUESTION: quotes the most relevant passages of the top document.
 * - EXPLAIN_FLOW: rebuilds the document structure as a step-by-step walkthrough.
 * - IMPROVE_DOCUMENT: audits health and explicitly refuses to rewrite
 *   healthy documents, explaining why.
 * - SUMMARIZE: condenses the top document into its key points.
 * - CREATE_DOCUMENT: handled by `CreateDocumentFromAssistantUseCase`; this
 *   branch only fires if the engine is asked directly without going through it.
 */
class GroundedAssistantEngine(
    private val shouldImproveDocument: ShouldImproveDocumentUseCase,
    private val markdownParser: MarkdownParser = MarkdownParser(),
    private val detectLanguage: DetectAssistantLanguageUseCase = DetectAssistantLanguageUseCase(),
) : AssistantEngine {

    override suspend fun answer(
        question: String,
        intent: AssistantIntentType,
        grounding: List<ScoredDocument>,
    ): DomainResult<AssistantAnswer> {
        val language = detectLanguage(question)
        if (grounding.isEmpty()) {
            return DomainResult.success(notEnoughInformation(intent, language))
        }
        val top = grounding.first()
        val markdown = when (intent) {
            AssistantIntentType.QUESTION -> answerQuestion(question, top.document, language)
            AssistantIntentType.EXPLAIN_FLOW -> explainFlow(top.document, language)
            AssistantIntentType.IMPROVE_DOCUMENT -> adviseOnImprovement(top.document, language)
            AssistantIntentType.SUMMARIZE -> summarize(top.document, language)
            AssistantIntentType.CREATE_DOCUMENT -> Copy.of(language).createDocumentHint
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

    private fun answerQuestion(question: String, document: Document, language: AssistantLanguage): String {
        val copy = Copy.of(language)
        val blocks = blocksOf(document)
        val terms = question.lowercase().split(' ').filter { it.length >= 4 }
        val relevantParagraphs = blocks
            .filterIsInstance<ParagraphBlock>()
            .filter { paragraph -> terms.any { it in paragraph.text.lowercase() } }
            .take(2)
        return buildString {
            appendLine(copy.questionIntro(document.title))
            appendLine()
            if (relevantParagraphs.isEmpty()) {
                appendLine(document.summary.ifBlank { firstParagraph(blocks, copy) })
            } else {
                relevantParagraphs.forEach { appendLine(it.text); appendLine() }
            }
            append(copy.questionOutro)
        }.trim()
    }

    private fun explainFlow(document: Document, language: AssistantLanguage): String {
        val copy = Copy.of(language)
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
            appendLine(copy.flowIntro(document.title))
            appendLine()
            if (sections.isEmpty()) {
                appendLine(document.summary.ifBlank { firstParagraph(blocks, copy) })
            } else {
                sections.forEachIndexed { index, (heading, steps) ->
                    appendLine("${index + 1}. **$heading**")
                    steps.take(MAX_STEPS_PER_SECTION).forEach { appendLine("   - $it") }
                }
            }
            appendLine()
            append(copy.flowOutro)
        }.trim()
    }

    private fun adviseOnImprovement(document: Document, language: AssistantLanguage): String {
        val copy = Copy.of(language)
        val decision = shouldImproveDocument(document)
        return if (!decision.shouldImprove) {
            buildString {
                appendLine(copy.notRewriteIntro(document.title))
                appendLine()
                appendLine(copy.healthScoreLabel(decision.healthScore))
                decision.reasons.forEach { appendLine("- $it") }
            }.trim()
        } else {
            buildString {
                appendLine(copy.needsUpdateIntro(document.title, decision.healthScore))
                appendLine()
                appendLine(copy.whatsWrong)
                decision.reasons.forEach { appendLine("- $it") }
                appendLine()
                appendLine(copy.suggestedImprovements)
                decision.suggestions.forEach { appendLine("- $it") }
            }.trim()
        }
    }

    private fun summarize(document: Document, language: AssistantLanguage): String {
        val copy = Copy.of(language)
        val blocks = blocksOf(document)
        val keyPoints = blocks.filterIsInstance<ListBlock>().flatMap { it.items }.take(MAX_SUMMARY_POINTS)
        return buildString {
            appendLine(copy.summaryHeader(document.title))
            appendLine()
            appendLine(document.summary.ifBlank { firstParagraph(blocks, copy) })
            if (keyPoints.isNotEmpty()) {
                appendLine()
                appendLine(copy.keyPoints)
                keyPoints.forEach { appendLine("- $it") }
            }
        }.trim()
    }

    private fun notEnoughInformation(intent: AssistantIntentType, language: AssistantLanguage) = AssistantAnswer(
        markdown = Copy.of(language).notEnoughInformation,
        confidence = AnswerConfidence.NOT_ENOUGH_INFORMATION,
        sources = emptyList(),
        intent = intent,
    )

    private fun blocksOf(document: Document) =
        document.content.blocks.ifEmpty { markdownParser.parse(document.rawMarkdown).content.blocks }

    private fun firstParagraph(blocks: List<com.shelldocs.core.domain.entity.document.ContentBlock>, copy: Copy): String =
        blocks.filterIsInstance<ParagraphBlock>().firstOrNull()?.text
            ?: copy.noNarrativeContent

    private fun breadcrumb(document: Document): String = listOf(
        document.attributes.team.ifBlank { document.attributes.platform },
        document.attributes.module,
    ).filter { it.isNotBlank() }.joinToString(" / ")

    /** Localized templates for the deterministic engine's static copy (EN/ES/FR). */
    private class Copy(
        val questionIntro: (String) -> String,
        val questionOutro: String,
        val flowIntro: (String) -> String,
        val flowOutro: String,
        val notRewriteIntro: (String) -> String,
        val healthScoreLabel: (Int) -> String,
        val needsUpdateIntro: (String, Int) -> String,
        val whatsWrong: String,
        val suggestedImprovements: String,
        val summaryHeader: (String) -> String,
        val keyPoints: String,
        val noNarrativeContent: String,
        val createDocumentHint: String,
        val notEnoughInformation: String,
    ) {
        companion object {
            fun of(language: AssistantLanguage): Copy = when (language) {
                AssistantLanguage.SPANISH -> SPANISH
                AssistantLanguage.FRENCH -> FRENCH
                AssistantLanguage.ENGLISH -> ENGLISH
            }

            private val ENGLISH = Copy(
                questionIntro = { title -> "Here's what **$title** says about that:" },
                questionOutro = "If you need the full context, open the source below.",
                flowIntro = { title -> "Here's how **$title** works, step by step:" },
                flowOutro = "Each stage is documented in detail in the cited source.",
                notRewriteIntro = { title -> "I reviewed **$title** and I would *not* rewrite it right now." },
                healthScoreLabel = { score -> "Health score: **$score/100**." },
                needsUpdateIntro = { title, score -> "**$title** does need an update — health score **$score/100**." },
                whatsWrong = "What's wrong:",
                suggestedImprovements = "Suggested improvements:",
                summaryHeader = { title -> "**$title** — summary:" },
                keyPoints = "Key points:",
                noNarrativeContent = "The document has no narrative content yet.",
                createDocumentHint = "I can draft that document for you — ask again with " +
                    "\"create a document about ...\" and I'll set it up as a new draft.",
                notEnoughInformation = "I don't have indexed documentation on that yet. You can try other terms " +
                    "(for example *authentication*, *loyalty rewards*, *release process*), or if you'd " +
                    "rather start from scratch I can create a draft for you right now — just say " +
                    "\"create a document about ...\" and I'll set up the initial structure.",
            )

            private val SPANISH = Copy(
                questionIntro = { title -> "Esto es lo que dice **$title** sobre eso:" },
                questionOutro = "Si necesitas mas contexto, abre la fuente abajo.",
                flowIntro = { title -> "Asi es como funciona **$title**, paso a paso:" },
                flowOutro = "Cada etapa esta documentada en detalle en la fuente citada.",
                notRewriteIntro = { title -> "Revise **$title** y *no* lo reescribiria ahora mismo." },
                healthScoreLabel = { score -> "Puntuacion de salud: **$score/100**." },
                needsUpdateIntro = { title, score -> "**$title** si necesita una actualizacion — puntuacion de salud **$score/100**." },
                whatsWrong = "Que esta mal:",
                suggestedImprovements = "Mejoras sugeridas:",
                summaryHeader = { title -> "**$title** — resumen:" },
                keyPoints = "Puntos clave:",
                noNarrativeContent = "El documento aun no tiene contenido narrativo.",
                createDocumentHint = "Puedo crear ese borrador por ti — pidemelo de nuevo con " +
                    "\"crea un documento sobre ...\" y te armo la estructura inicial.",
                notEnoughInformation = "Todavia no tengo documentacion indexada sobre eso. Puedes intentar con otros " +
                    "terminos (por ejemplo *autenticacion*, *recompensas*, *proceso de release*), o si quieres " +
                    "puedo crearte un borrador nuevo ahora mismo — solo dime \"crea un documento sobre ...\" y " +
                    "te armo la estructura inicial para que la completes.",
            )

            private val FRENCH = Copy(
                questionIntro = { title -> "Voici ce que **$title** dit a ce sujet :" },
                questionOutro = "Si tu as besoin de plus de contexte, ouvre la source ci-dessous.",
                flowIntro = { title -> "Voici comment fonctionne **$title**, etape par etape :" },
                flowOutro = "Chaque etape est documentee en detail dans la source citee.",
                notRewriteIntro = { title -> "J'ai examine **$title** et je ne le reecrirais *pas* maintenant." },
                healthScoreLabel = { score -> "Score de sante : **$score/100**." },
                needsUpdateIntro = { title, score -> "**$title** a besoin d'une mise a jour — score de sante **$score/100**." },
                whatsWrong = "Ce qui ne va pas :",
                suggestedImprovements = "Ameliorations suggerees :",
                summaryHeader = { title -> "**$title** — resume :" },
                keyPoints = "Points cles :",
                noNarrativeContent = "Le document n'a pas encore de contenu narratif.",
                createDocumentHint = "Je peux rediger ce document pour toi — redemande avec " +
                    "\"cree un document sur ...\" et je preparerai un brouillon.",
                notEnoughInformation = "Je n'ai pas encore de documentation indexee sur ce sujet. Tu peux essayer " +
                    "d'autres termes (par exemple *authentification*, *recompenses*, *processus de release*), ou " +
                    "si tu preferes je peux creer un brouillon des maintenant — dis simplement " +
                    "\"cree un document sur ...\" et je prepare la structure initiale.",
            )
        }
    }

    private companion object {
        const val MAX_STEPS_PER_SECTION = 4
        const val MAX_SUMMARY_POINTS = 5
    }
}
