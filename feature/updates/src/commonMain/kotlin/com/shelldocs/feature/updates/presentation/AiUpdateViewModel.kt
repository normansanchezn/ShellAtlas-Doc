package com.shelldocs.feature.updates.presentation

import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.error.toErrorDialogState
import com.shelldocs.core.common.mvi.MviViewModel
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.UserProfile
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.usecase.document.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Documentation review workflow, in order:
 * pre-analysis -> preview + single continuous editor -> Save Changes (draft)
 * -> Metadata Review -> Apply Update -> final confirmation -> sync pipeline.
 *
 * Pre-analysis and the sync targets (Confluence, Azure DevOps, Search Index)
 * aren't wired to real systems yet; stages are simulated with a short delay
 * so the staged progress UI is real even though the backing calls aren't.
 */
class AiUpdateViewModel(
    private val generateSuggestedUpdate: GenerateSuggestedUpdateUseCase,
    private val saveDraft: SaveDraftUseCase,
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
            is AiUpdateIntent.Initialize -> analyzeAndGenerate(intent.documentId)
            is AiUpdateIntent.EditMarkdown -> setState { copy(suggestedMarkdown = intent.markdown) }
            AiUpdateIntent.SaveChanges -> saveChanges()
            is AiUpdateIntent.ConfirmMetadata ->
                setState {
                    copy(
                        metadataDraft = intent.attributes,
                        showMetadataDialog = false,
                        showConfirmDialog = true
                    )
                }

            AiUpdateIntent.CancelMetadata -> setState { copy(showMetadataDialog = false) }
            AiUpdateIntent.ConfirmApply -> apply()
            AiUpdateIntent.CancelApply -> setState { copy(showConfirmDialog = false) }
            AiUpdateIntent.DismissError -> setState { copy(errorDialog = null) }
        }
    }

    private suspend fun analyzeAndGenerate(documentId: String) {
        setState { copy(errorDialog = null) }
        for (stage in AnalysisStage.entries) {
            setState { copy(analysisStage = stage) }
            if (stage != AnalysisStage.GENERATING_SUGGESTED_CHANGES) delay(SIMULATED_STAGE_DELAY_MS)
        }
        withContext(dispatchers.default) {
            generateSuggestedUpdate(documentId)
        }
            .onResult(
                onSuccess = { suggestion ->
                    setState {
                        copy(
                            analysisStage = null,
                            documentId = suggestion.documentId,
                            documentTitle = suggestion.documentTitle,
                            attributes = suggestion.attributes,
                            currentContentBlocks = suggestion.currentContentBlocks,
                            suggestedMarkdown = suggestion.suggestedMarkdown,
                        )
                    }
                },
                onFailure = { error ->
                    setState {
                        copy(
                            analysisStage = null,
                            errorDialog = error.toErrorDialogState("analyze this document")
                        )
                    }
                },
            )
    }

    private suspend fun saveChanges() {
        withContext(dispatchers.default) {
            saveDraft(currentState.documentId, currentState.suggestedMarkdown)
        }
            .onResult(
                onSuccess = { setState { copy(showMetadataDialog = true, metadataDraft = attributes) } },
                onFailure = { error -> setState { copy(errorDialog = error.toErrorDialogState("save your changes")) } },
            )
    }

    private suspend fun apply() {
        setState { copy(showConfirmDialog = false, errorDialog = null) }
        val role = roleProvider()
        val documentId = currentState.documentId
        val changeSummary = buildChangeSummary()

        setState { copy(applyStage = ApplyStage.UPDATING_DATABASE) }
        val attributesResult =
            withContext(dispatchers.default) { updateAttributes(role, documentId, currentState.metadataDraft) }
        if (attributesResult is DomainResult.Failure) {
            setState {
                copy(
                    applyStage = null,
                    errorDialog = attributesResult.error.toErrorDialogState("update the internal database")
                )
            }
            return
        }
        runStage(ApplyStage.UPDATING_DATABASE, "save this document") {
            publishDocument(role, documentId, currentState.suggestedMarkdown, changeSummary)
        } ?: return

        runStage(ApplyStage.SYNCING_CONFLUENCE, "update Confluence") {
            syncToSourcesOfTruth.confluence(documentId)
        } ?: return

        runStage(ApplyStage.UPDATING_AZURE_DEVOPS, "update Azure DevOps") {
            syncToSourcesOfTruth.azureDevOpsWiki(documentId)
        } ?: return

        runStage(ApplyStage.UPDATING_SEARCH_INDEX, "update the search index") {
            syncToSourcesOfTruth.reindexSearch(documentId)
        } ?: return

        // Version entry is already created as part of publishDocument() above; this stage is
        // shown for UX parity with the spec's pipeline diagram, not a second backend call.
        setState { copy(applyStage = ApplyStage.CREATING_VERSION_ENTRY) }
        delay(SIMULATED_STAGE_DELAY_MS)

        setState { copy(applyStage = null) }
        sendEffect(AiUpdateEffect.UpdateApplied)
    }

    /** Sets [stage], runs [block], and on failure parks the error and clears the stage. Returns the value, or null on failure. */
    private suspend fun <T> runStage(stage: ApplyStage, action: String, block: suspend () -> DomainResult<T>): T? {
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
        return "Applied AI-suggested update" + (actor?.let { " by ${it.fullName} (${it.role.displayName})" } ?: "")
    }

    private inline fun <T> DomainResult<T>.onResult(onSuccess: (T) -> Unit, onFailure: (AppError) -> Unit) {
        when (this) {
            is DomainResult.Success -> onSuccess(value)
            is DomainResult.Failure -> onFailure(error)
        }
    }

    private companion object {
        const val SIMULATED_STAGE_DELAY_MS = 350L
    }
}
