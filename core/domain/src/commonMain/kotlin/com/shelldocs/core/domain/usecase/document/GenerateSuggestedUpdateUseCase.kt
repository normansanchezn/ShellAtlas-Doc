@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.core.domain.usecase.document

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.domain.entity.assistant.DocumentHealth
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.DocumentUpdateSuggestion
import com.shelldocs.core.domain.repository.DocumentRepository
import com.shelldocs.core.domain.usecase.assistant.EvaluateDocumentHealthUseCase

/**
 * Deterministic stand-in for AI-assisted document remediation. It never emits
 * audit bullets as the suggested content; instead it either keeps the current
 * document unchanged or proposes documentation-ready markdown updates.
 */
class GenerateSuggestedUpdateUseCase(
    private val documentRepository: DocumentRepository,
    private val evaluateHealth: EvaluateDocumentHealthUseCase,
    private val timeProvider: TimeProvider,
) {

    suspend operator fun invoke(documentId: String): DomainResult<DocumentUpdateSuggestion> {
        val document = when (val result = documentRepository.document(documentId)) {
            is DomainResult.Success -> result.value
            is DomainResult.Failure -> return result
        }
        val allDocuments = when (val result = documentRepository.documents()) {
            is DomainResult.Success -> result.value
            is DomainResult.Failure -> return DomainResult.failure(result.error)
        }
        return DomainResult.success(toSuggestion(document, allDocuments))
    }

    private fun toSuggestion(
        document: Document,
        allDocuments: List<Document>,
    ): DocumentUpdateSuggestion {
        val health = evaluateHealth(document)
        val relatedContext = relatedContext(document, allDocuments)
        val suggestedMarkdown = buildSuggestedMarkdown(document, health, relatedContext).trim()

        return DocumentUpdateSuggestion(
            documentId = document.id,
            documentTitle = document.title,
            attributes = document.attributes,
            currentContentBlocks = document.content.blocks,
            currentMarkdown = document.rawMarkdown,
            suggestedMarkdown = if (suggestedMarkdown.isBlank()) document.rawMarkdown else suggestedMarkdown,
            generatedAt = timeProvider.now(),
        )
    }

    private fun buildSuggestedMarkdown(
        document: Document,
        health: DocumentHealth,
        context: RelatedContext,
    ): String {
        if (shouldLeaveUnchanged(document, health, context)) return document.rawMarkdown

        val signals = relevantSignals(document, context)
        if (requiresFullRewrite(document, health, context)) {
            return fullRewrite(document, context, signals)
        }

        return when {
            document.isReleaseLike -> updateReleaseDocument(document, context, signals)
            document.isAuthenticationLike -> updateAuthenticationDocument(document, context, signals)
            else -> updateGenericDocument(document, context, signals)
        }
    }

    private fun shouldLeaveUnchanged(
        document: Document,
        health: DocumentHealth,
        context: RelatedContext,
    ): Boolean {
        if (health.isHealthy) return true
        val issuesRequireTextChange =
            document.rawMarkdown.contains("TODO", ignoreCase = true) ||
                    document.rawMarkdown.contains("TBD", ignoreCase = true) ||
                    document.plainText.length < THIN_REWRITE_THRESHOLD ||
                    context.versionMismatch ||
                    context.relatedDocuments.isNotEmpty()
        return !issuesRequireTextChange
    }

    private fun requiresFullRewrite(
        document: Document,
        health: DocumentHealth,
        context: RelatedContext,
    ): Boolean =
        document.plainText.length < FULL_REWRITE_THRESHOLD &&
                (health.issues.size >= 2 || context.versionMismatch)

    private fun updateReleaseDocument(
        document: Document,
        context: RelatedContext,
        signals: List<String>,
    ): String {
        var updated = stripAuditBullets(removeTodoLines(document.rawMarkdown))
        val latestVersion = context.latestApplicationVersion ?: document.attributes.applicationVersion
        val buildGeneration = buildList {
            add("1. Update `build.gradle.kts` version values for application version ${latestVersion.ifBlank { "the current release" }}.")
            add("2. Verify `updateconfig.py`, generated versionCodes and branch-specific release configuration.")
            if (signals.any { "GitHub Actions" in it }) add("3. Run the GitHub Actions release workflow and confirm the QA handoff artifact is attached.")
            if (signals.any { "Azure" in it }) add("4. Re-validate Azure-managed secrets before promoting the build.")
        }
        val validation = buildList {
            add("- Confirm smoke tests on pilot branches.")
            if (signals.any { "Credential Manager" in it }) add("- Verify Credential Manager migration validation before sign-off.")
            if (signals.any { "Lokalise" in it }) add("- Review Lokalise changes and translation coverage before QA sign-off.")
            if (latestVersion.isNotBlank()) add("- Validate release configuration against application version $latestVersion.")
            if (signals.any { "Release Notes" in it }) add("- Confirm release notes reflect user-visible release or localization changes.")
        }
        val rollback = buildString {
            appendLine("## Rollback Process")
            appendLine()
            appendLine("If pilot validation fails:")
            appendLine()
            appendLine("1. Revert the release candidate tag or branch promotion.")
            appendLine("2. Restore the previous production configuration and version metadata.")
            appendLine("3. Execute the rollback workflow from GitHub Actions.")
            append("4. Notify QA, Release Management and the owning mobile team before resuming validation.")
        }

        updated = replaceOrAppendSection(updated, "Build Generation", buildGeneration.joinToString("\n"))
        updated = replaceOrAppendSection(updated, "Validation", validation.joinToString("\n"))
        updated =
            replaceOrAppendSection(updated, "Rollback Process", rollback.substringAfter("## Rollback Process\n\n"))
        return updated
    }

    private fun updateAuthenticationDocument(
        document: Document,
        context: RelatedContext,
        signals: List<String>,
    ): String {
        var updated = stripAuditBullets(removeTodoLines(document.rawMarkdown))
        val intro = buildString {
            appendLine("# ${document.title}")
            appendLine()
            append("This document describes the current authentication flow for ${document.attributes.platform.ifBlank { "the mobile app" }}")
            if (signals.any { "Credential Manager" in it }) {
                append(", including Credential Manager validation and token-storage expectations.")
            } else {
                append(".")
            }
        }
        updated = replaceTitleBlock(updated, intro)
        val currentState = buildList {
            add("- Credential Manager handles passkey and password sign-in.")
            add("- Tokens are stored in encrypted platform storage and rotated during silent refresh.")
            if (context.latestApplicationVersion != null) {
                add("- Validate auth behavior against application version ${context.latestApplicationVersion}.")
            }
        }
        val validation = buildList {
            add("- Verify sign-in with passkeys and password fallback.")
            add("- Confirm token refresh succeeds after session expiry thresholds.")
            add("- Re-check localization and release notes if the login copy or recovery path changed.")
        }
        updated = replaceOrAppendSection(updated, "Current State", currentState.joinToString("\n"))
        updated = replaceOrAppendSection(updated, "Validation", validation.joinToString("\n"))
        return updated
    }

    private fun updateGenericDocument(
        document: Document,
        context: RelatedContext,
        signals: List<String>,
    ): String {
        var updated = stripAuditBullets(removeTodoLines(document.rawMarkdown))
        if (document.plainText.length < THIN_REWRITE_THRESHOLD) {
            val operationalNotes = buildList {
                add("This document was refreshed to include the operational details needed for implementation and review.")
                context.latestApplicationVersion?.let { add("The current validated application version is $it.") }
                signals.take(3).forEach { add(it) }
            }
            updated = replaceOrAppendSection(updated, "Operational Notes", operationalNotes.joinToString("\n\n"))
        }
        if (signals.isNotEmpty()) {
            val validation = buildList {
                add("- Re-check the documented flow against the current implementation and related documents.")
                signals.take(3).forEach { add("- $it") }
            }
            updated = replaceOrAppendSection(updated, "Validation", validation.joinToString("\n"))
        }
        return updated
    }

    private fun fullRewrite(
        document: Document,
        context: RelatedContext,
        signals: List<String>,
    ): String {
        val scopePlatform = document.attributes.platform.ifBlank { "the product" }
        val latestVersion = context.latestApplicationVersion?.let { "Application version: $it." } ?: ""
        val signalLines = signals.take(4).joinToString("\n") { "- $it" }
        return """
            # ${document.title}

            ${document.summary.ifBlank { "This document captures the current workflow and validation expectations for $scopePlatform." }}

            ## Scope

            This guide reflects the current implementation for ${document.attributes.module.ifBlank { document.title }}. $latestVersion

            ## Current Workflow

            ${context.workflowSummary(document)}

            ## Validation

            - Confirm the documented steps against the current implementation.
            - Re-run the release or functional checks required by the owning team.
            - Verify dependent configuration, localization and environment values before publishing changes.

            ## Related Signals

            $signalLines
        """.trimIndent()
    }

    private fun relatedContext(
        document: Document,
        allDocuments: List<Document>,
    ): RelatedContext {
        val candidates = allDocuments
            .filter { it.id != document.id }
            .mapNotNull { candidate ->
                val score = relationScore(document, candidate)
                candidate.takeIf { score > 0 }?.let { score to it }
            }
            .sortedByDescending { it.first }
            .map { it.second }

        val latestVersion = semanticVersions(
            listOfNotNull(document.attributes.applicationVersion) +
                    allDocuments.map { it.attributes.applicationVersion }.filter { it.isNotBlank() }
        ).maxWithOrNull(compareBy({ it.major }, { it.minor }, { it.patch }))?.raw

        return RelatedContext(
            relatedDocuments = candidates.take(MAX_RELATED_DOCUMENTS),
            latestApplicationVersion = latestVersion,
            versionMismatch = latestVersion != null &&
                    document.attributes.applicationVersion.isNotBlank() &&
                    document.attributes.applicationVersion != latestVersion,
        )
    }

    private fun relationScore(document: Document, candidate: Document): Int {
        var score = 0
        if (candidate.attributes.area != null && candidate.attributes.area == document.attributes.area) score += 4
        if (candidate.attributes.module.equals(document.attributes.module, ignoreCase = true)) score += 4
        if (candidate.attributes.platform.equals(document.attributes.platform, ignoreCase = true)) score += 2
        if (candidate.attributes.applicationVersion == document.attributes.applicationVersion && candidate.attributes.applicationVersion.isNotBlank()) score += 2
        score += document.attributes.tags.intersect(candidate.attributes.tags.toSet()).size * 3
        if (document.isReleaseLike && candidate.rawMarkdown.contains("release", ignoreCase = true)) score += 2
        if (document.rawMarkdown.contains("Lokalise", ignoreCase = true) && candidate.rawMarkdown.contains(
                "Lokalise",
                ignoreCase = true
            )
        ) score += 2
        if (document.rawMarkdown.contains("Azure", ignoreCase = true) && candidate.rawMarkdown.contains(
                "Azure",
                ignoreCase = true
            )
        ) score += 2
        return score
    }

    private fun relevantSignals(
        document: Document,
        context: RelatedContext,
    ): List<String> = buildList {
        if (context.versionMismatch && context.latestApplicationVersion != null) {
            add("Update version references to align with application version ${context.latestApplicationVersion}.")
        }
        context.relatedDocuments.forEach { related ->
            when {
                related.title.contains("Lokalise", ignoreCase = true) ->
                    add("Review Lokalise export and translation validation referenced in ${related.title}.")

                related.title.contains("Azure Secrets", ignoreCase = true) ->
                    add("Re-check Azure DevOps or Azure-managed secret values referenced by ${related.title}.")

                related.title.contains("Release Process", ignoreCase = true) ->
                    add("Align QA handoff and release-note steps with ${related.title}.")

                related.title.contains("Authentication", ignoreCase = true) &&
                        related.rawMarkdown.contains("Credential Manager", ignoreCase = true) ->
                    add("Include Credential Manager validation in the documented flow.")
            }
        }
        if (document.rawMarkdown.contains("Confluence", ignoreCase = true).not() && document.isReleaseLike) {
            add("Confirm the latest Confluence and Azure DevOps references match the documented release path.")
        }
    }.distinct()

    private fun replaceOrAppendSection(markdown: String, heading: String, body: String): String {
        val normalizedBody = body.trim()
        val regex = Regex("""(?ms)^## ${Regex.escape(heading)}\n+(.*?)(?=^## |\z)""")
        return if (regex.containsMatchIn(markdown)) {
            markdown.replace(regex, "## $heading\n\n$normalizedBody\n\n")
        } else {
            markdown.trimEnd() + "\n\n## $heading\n\n$normalizedBody"
        }.trimEnd()
    }

    private fun replaceTitleBlock(markdown: String, titleBlock: String): String {
        val regex = Regex("""(?ms)^# .*$.*?(?=^## |\z)""")
        return if (regex.containsMatchIn(markdown)) {
            markdown.replace(regex, titleBlock.trim() + "\n\n")
        } else {
            titleBlock.trim() + "\n\n" + markdown.trimStart()
        }
    }

    private fun removeTodoLines(markdown: String): String =
        markdown
            .lines()
            .filterNot { line -> TODO_MARKERS.any { marker -> marker in line.uppercase() } }
            .joinToString("\n")
            .replace(Regex("""\n{3,}"""), "\n\n")
            .trim()

    private fun stripAuditBullets(markdown: String): String =
        markdown.replace(Regex("""(?ms)^## AI Suggested Update\n.*?(?=^## |\z)"""), "").trim()

    private fun semanticVersions(values: List<String>): List<SemanticVersion> =
        values.mapNotNull { raw ->
            Regex("""(\d+)\.(\d+)\.(\d+)""").find(raw)?.destructured?.let { (major, minor, patch) ->
                SemanticVersion(raw, major.toInt(), minor.toInt(), patch.toInt())
            }
        }

    private data class RelatedContext(
        val relatedDocuments: List<Document>,
        val latestApplicationVersion: String?,
        val versionMismatch: Boolean,
    ) {
        fun workflowSummary(document: Document): String {
            val relatedModules =
                relatedDocuments.joinToString(", ") { it.attributes.module }.ifBlank { document.attributes.module }
            return buildString {
                append("Start from the current ")
                append(document.attributes.module.ifBlank { document.title })
                append(" workflow, then validate dependent steps across ")
                append(relatedModules.ifBlank { "related modules" })
                append(".")
            }
        }
    }

    private data class SemanticVersion(
        val raw: String,
        val major: Int,
        val minor: Int,
        val patch: Int,
    )

    private companion object {
        const val MAX_RELATED_DOCUMENTS = 4
        const val THIN_REWRITE_THRESHOLD = 220
        const val FULL_REWRITE_THRESHOLD = 120
        val TODO_MARKERS = listOf("TODO", "TBD", "FIXME", "WIP")
    }
}

private val Document.isReleaseLike: Boolean
    get() = title.contains("release", ignoreCase = true) ||
            title.contains("eosb", ignoreCase = true) ||
            attributes.tags.any {
                it.contains("release", ignoreCase = true) ||
                        it.contains("eosb", ignoreCase = true) ||
                        it.contains("pilot", ignoreCase = true) ||
                        it.contains("qa", ignoreCase = true)
            }

private val Document.isAuthenticationLike: Boolean
    get() = title.contains("auth", ignoreCase = true) ||
            attributes.tags.any {
                it.contains("auth", ignoreCase = true) ||
                        it.contains("credential-manager", ignoreCase = true)
            }
