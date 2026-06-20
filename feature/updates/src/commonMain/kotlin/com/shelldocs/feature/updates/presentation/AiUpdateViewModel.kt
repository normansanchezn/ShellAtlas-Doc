@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.feature.updates.presentation

import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.error.toErrorDialogState
import com.shelldocs.core.common.mvi.MviViewModel
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.result.onFailure
import com.shelldocs.core.common.result.onSuccess
import com.shelldocs.core.domain.entity.auth.UserProfile
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.entity.document.LineOrigin
import com.shelldocs.core.domain.usecase.document.GenerateSuggestedUpdateUseCase
import com.shelldocs.core.domain.usecase.document.PublishDocumentUseCase
import com.shelldocs.core.domain.usecase.document.SyncDocumentToSourcesOfTruthUseCase
import com.shelldocs.core.domain.usecase.document.UpdateDocumentAttributesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Apply Update runs the full Documentation Health sync pipeline:
 * Save Document -> Update Metadata -> Sync Confluence -> Update Azure DevOps
 * Wiki -> Reindex Search -> Success, each step its own [ApplyStage] so the UI
 * can show independent progress instead of one opaque spinner.
 */
class AiUpdateViewModel(
    private val generateSuggestedUpdate: GenerateSuggestedUpdateUseCase,
    private val publishDocument: PublishDocumentUseCase,
    private val updateAttributes: UpdateDocumentAttributesUseCase,
    private val syncToSourcesOfTruth: SyncDocumentToSourcesOfTruthUseCase,
    private val roleProvider: () -> UserRole,
    private val currentUserProvider: () -> UserProfile?,
    dispatchers: DispatcherProvider,
    documentIdRequests: StateFlow<String?> = MutableStateFlow(null),
    private val consumeDocumentIdRequest: () -> Unit = {},
) : MviViewModel<AiUpdateIntent, AiUpdateState, AiUpdateEffect>(
    AiUpdateState(isAdmin = roleProvider() == UserRole.OWNER),
    dispatchers,
) {

    init {
        scope.launch {
            documentIdRequests.collect { documentId ->
                if (documentId != null) {
                    onIntent(AiUpdateIntent.Initialize(documentId))
                    consumeDocumentIdRequest()
                }
            }
        }
    }

    override suspend fun handleIntent(intent: AiUpdateIntent) {
        when (intent) {
            is AiUpdateIntent.Initialize -> generate(intent.documentId)
            is AiUpdateIntent.EditLine -> editLine(intent.index, intent.text)
            is AiUpdateIntent.RemoveLine -> removeLine(intent.index)
            AiUpdateIntent.RequestApply -> setState { copy(showConfirmDialog = true) }
            AiUpdateIntent.CancelApply -> setState { copy(showConfirmDialog = false) }
            AiUpdateIntent.ConfirmApply -> apply()
            AiUpdateIntent.DismissError -> setState { copy(errorDialog = null) }
        }
    }

    private suspend fun generate(documentId: String) {
        setState { copy(isLoading = true, errorDialog = null) }
        withContext(dispatchers.default) {
            generateSuggestedUpdate(documentId)
        }
            .onSuccess { suggestion ->
                setState {
                    copy(
                        isLoading = false,
                        documentId = suggestion.documentId,
                        documentTitle = suggestion.documentTitle,
                        ownerName = suggestion.ownerName,
                        currentMarkdown = suggestion.currentMarkdown,
                        suggestedLines = suggestion.lines,
                    )
                }
            }
            .onFailure { error ->
                setState {
                    copy(
                        isLoading = false,
                        errorDialog = error.toErrorDialogState("generate the suggested update")
                    )
                }
            }
    }

    private fun editLine(index: Int, text: String) {
        setState {
            copy(
                suggestedLines = suggestedLines.mapIndexed { i, line ->
                    if (i != index) {
                        line
                    } else {
                        line.copy(
                            text = text,
                            origin = if (line.origin == LineOrigin.AI_SUGGESTED) LineOrigin.HUMAN_REVIEWED else line.origin,
                        )
                    }
                },
            )
        }
    }

    private fun removeLine(index: Int) {
        setState { copy(suggestedLines = suggestedLines.filterIndexed { i, _ -> i != index }) }
    }

    private suspend fun apply() {
        setState { copy(showConfirmDialog = false, errorDialog = null) }
        val role = roleProvider()
        val documentId = currentState.documentId
        val changeSummary = buildChangeSummary()

        val published = runStep(ApplyStage.SAVING_DOCUMENT, "save this document") {
            publishDocument(role, documentId, currentState.editedMarkdown, changeSummary)
        } ?: return

        runStep(ApplyStage.UPDATING_METADATA, "update document metadata") {
            updateAttributes(role, documentId, published.attributes.copy(lastReviewedDate = published.updatedAt))
        } ?: return

        runStep(ApplyStage.SYNCING_CONFLUENCE, "sync Confluence") {
            syncToSourcesOfTruth.confluence(documentId)
        } ?: return

        runStep(ApplyStage.UPDATING_AZURE_DEVOPS, "update Azure DevOps Wiki") {
            syncToSourcesOfTruth.azureDevOpsWiki(documentId)
        } ?: return

        runStep(ApplyStage.REINDEXING_SEARCH, "reindex search") {
            syncToSourcesOfTruth.reindexSearch(documentId)
        } ?: return

        setState { copy(applyStage = null) }
        sendEffect(AiUpdateEffect.UpdateApplied)
    }

    /** Sets [stage], runs [block], and on failure parks the error and clears the stage. Returns the value, or null on failure. */
    private suspend fun <T> runStep(stage: ApplyStage, action: String, block: suspend () -> DomainResult<T>): T? {
        setState { copy(applyStage = stage) }
        return when (val result = withContext(dispatchers.default) { block() }) {
            is DomainResult.Success -> result.value
            is DomainResult.Failure -> {
                setState { copy(applyStage = null, errorDialog = result.error.toErrorDialogState(action)) }
                null
            }
        }
    }

    private fun buildChangeSummary(): String {
        val actor = currentUserProvider()
        val modifiedSections = currentState.suggestedLines
            .filter { it.origin != LineOrigin.ORIGINAL && it.text.isNotBlank() && !it.text.startsWith("#") }
        val header =
            "Applied AI-suggested update" + (actor?.let { " by ${it.fullName} (${it.role.displayName})" } ?: "")
        return if (modifiedSections.isEmpty()) {
            header
        } else {
            header + "\nChanges:\n" + modifiedSections.joinToString("\n") { "- ${it.text.removePrefix("- ")}" }
        }
    }
}
