package com.shelldocs.core.domain.usecase.assistant

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.assistant.AnswerConfidence
import com.shelldocs.core.domain.entity.assistant.AnswerSource
import com.shelldocs.core.domain.entity.assistant.AssistantAnswer
import com.shelldocs.core.domain.entity.assistant.AssistantIntentType
import com.shelldocs.core.domain.entity.assistant.AssistantLanguage
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.usecase.document.CreateDocumentUseCase

/**
 * Lets the assistant turn a chat request directly into a new draft document,
 * so "create a doc about X" is an action, not a dead end. Replies in the
 * language the request was written in (EN/ES/FR).
 */
class CreateDocumentFromAssistantUseCase(
    private val createDocument: CreateDocumentUseCase,
    private val detectLanguage: DetectAssistantLanguageUseCase = DetectAssistantLanguageUseCase(),
) {

    suspend operator fun invoke(
        actorRole: UserRole,
        request: String,
        language: AssistantLanguage? = null,
    ): DomainResult<AssistantAnswer> {
        val title = titleFrom(request)
        val resolvedLanguage = language ?: detectLanguage(request)
        val copy = Copy.of(resolvedLanguage)
        val markdown = draftMarkdown(title, request, copy)

        return when (val result = createDocument(actorRole, title, markdown)) {
            is DomainResult.Success -> {
                val document = result.value
                DomainResult.success(
                    AssistantAnswer(
                        markdown = copy.created(document.title),
                        confidence = AnswerConfidence.HIGH,
                        sources = listOf(
                            AnswerSource(
                                documentId = document.id,
                                title = document.title,
                                breadcrumb = copy.draftsLabel,
                                relevance = 100,
                            ),
                        ),
                        intent = AssistantIntentType.CREATE_DOCUMENT,
                    ),
                )
            }
            is DomainResult.Failure -> DomainResult.success(failureAnswer(result.error, copy))
        }
    }

    private fun failureAnswer(error: AppError, copy: Copy): AssistantAnswer {
        val markdown = when (error) {
            is AppError.Unauthorized -> copy.unauthorized
            else -> copy.failed(error.message)
        }
        return AssistantAnswer(
            markdown = markdown,
            confidence = AnswerConfidence.NOT_ENOUGH_INFORMATION,
            sources = emptyList(),
            intent = AssistantIntentType.CREATE_DOCUMENT,
        )
    }

    private fun titleFrom(request: String): String {
        var remainder = request.trim()
        TRIGGER_PATTERNS.forEach { pattern ->
            remainder = pattern.replace(remainder, "")
        }
        remainder = remainder.trim().trim('"', '\'', '.', '!', '?')
        if (remainder.isBlank()) return "Untitled draft"
        return remainder.replaceFirstChar { it.uppercase() }.take(MAX_TITLE_LENGTH)
    }

    private fun draftMarkdown(title: String, request: String, copy: Copy): String {
        return buildString {
            appendLine("# $title")
            appendLine()
            appendLine("## ${copy.summaryHeading}")
            appendLine()
            appendLine(copy.generatedFromRequest)
            appendLine("> $request")
            appendLine()
            appendLine("## ${copy.detailsHeading}")
            appendLine()
            appendLine(copy.addDetailsHere)
            appendLine()
            appendLine("## ${copy.openQuestionsHeading}")
            appendLine()
            appendLine("- ")
        }
    }

    /** Localized templates for the document-creation flow (EN/ES/FR). */
    private class Copy(
        val created: (String) -> String,
        val unauthorized: String,
        val failed: (String) -> String,
        val draftsLabel: String,
        val summaryHeading: String,
        val detailsHeading: String,
        val openQuestionsHeading: String,
        val generatedFromRequest: String,
        val addDetailsHere: String,
    ) {
        companion object {
            fun of(language: AssistantLanguage): Copy = when (language) {
                AssistantLanguage.SPANISH -> SPANISH
                AssistantLanguage.FRENCH -> FRENCH
                AssistantLanguage.ENGLISH -> ENGLISH
            }

            private val ENGLISH = Copy(
                created = { title ->
                    "Done — I created a draft called **$title** with the initial structure you asked for. " +
                        "Open it from Documents to fill it in and publish when ready."
                },
                unauthorized = "Your current role can't create documents. Ask an Owner or someone on the " +
                    "Develop team to create it, or to grant you edit permissions.",
                failed = { message -> "I couldn't create the document ($message). Please try again in a moment." },
                draftsLabel = "Drafts",
                summaryHeading = "Summary",
                detailsHeading = "Details",
                openQuestionsHeading = "Open Questions",
                generatedFromRequest = "_Draft generated by the assistant from this request:_",
                addDetailsHere = "Add the relevant details here.",
            )

            private val SPANISH = Copy(
                created = { title ->
                    "Listo, cree un borrador llamado **$title** con la estructura inicial que pediste. " +
                        "Puedes abrirlo desde Documentos para completarlo y publicarlo cuando este listo."
                },
                unauthorized = "Tu rol actual no tiene permiso para crear documentos. Pidele a un Owner o a " +
                    "alguien del equipo de Develop que lo cree, o que te de permisos de edicion.",
                failed = { message -> "No pude crear el documento ($message). Intenta de nuevo en un momento." },
                draftsLabel = "Borradores",
                summaryHeading = "Resumen",
                detailsHeading = "Detalles",
                openQuestionsHeading = "Preguntas abiertas",
                generatedFromRequest = "_Borrador generado por el asistente a partir de esta solicitud:_",
                addDetailsHere = "Agrega aqui los detalles relevantes.",
            )

            private val FRENCH = Copy(
                created = { title ->
                    "C'est fait — j'ai cree un brouillon appele **$title** avec la structure initiale " +
                        "demandee. Ouvre-le depuis Documents pour le completer et le publier quand il sera pret."
                },
                unauthorized = "Ton role actuel ne permet pas de creer des documents. Demande a un Owner ou a " +
                    "quelqu'un de l'equipe Develop de le creer, ou de t'accorder les droits d'edition.",
                failed = { message -> "Je n'ai pas pu creer le document ($message). Reessaie dans un instant." },
                draftsLabel = "Brouillons",
                summaryHeading = "Resume",
                detailsHeading = "Details",
                openQuestionsHeading = "Questions ouvertes",
                generatedFromRequest = "_Brouillon genere par l'assistant a partir de cette demande :_",
                addDetailsHere = "Ajoute ici les details pertinents.",
            )
        }
    }

    private companion object {
        const val MAX_TITLE_LENGTH = 80

        val TRIGGER_PATTERNS = listOf(
            Regex("create (a |an )?(new )?(doc(ument)?|page) (about|on|for|describing)?"),
            Regex("write( up)? (a |an )?(new )?doc(ument)? (about|on|for|describing)?"),
            Regex("draft (a |an )?(new )?doc(ument)? (about|on|for|describing)?"),
            Regex("(create|generate) (a |an )?new document"),
            Regex("document this:?"),
            Regex("crea(r)? (una |un )?(nuevo |nueva )?(documento|pagina|página) (sobre|de|para)?"),
            Regex("redacta (un )?(nuevo )?documento (sobre|de|para)?"),
            Regex("escribe (un )?(nuevo )?documento (sobre|de|para)?"),
            Regex("genera (un )?(nuevo )?documento (sobre|de|para)?"),
            Regex("documenta esto:?"),
            Regex("cree?(r)? (un |une )?(nouveau |nouvelle )?document (sur|de|pour)?"),
            Regex("redige(r)? (un |une )?(nouveau |nouvelle )?document (sur|de|pour)?"),
            Regex("genere(r)? (un |une )?(nouveau |nouvelle )?document (sur|de|pour)?"),
            Regex("documente ceci:?"),
        )
    }
}
