package com.shelldocs.feature.updates.presentation

import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.error.toErrorDialogState
import com.shelldocs.core.common.mvi.MviViewModel
import com.shelldocs.core.common.result.onFailure
import com.shelldocs.core.common.result.onSuccess
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.entity.document.LineOrigin
import com.shelldocs.core.domain.usecase.document.GenerateSuggestedUpdateUseCase
import com.shelldocs.core.domain.usecase.document.PublishDocumentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AiUpdateViewModel(
    private val generateSuggestedUpdate: GenerateSuggestedUpdateUseCase,
    private val publishDocument: PublishDocumentUseCase,
    private val roleProvider: () -> UserRole,
    dispatchers: DispatcherProvider,
    documentIdRequests: StateFlow<String?> = MutableStateFlow(null),
    private val consumeDocumentIdRequest: () -> Unit = {},
) : MviViewModel<AiUpdateIntent, AiUpdateState, AiUpdateEffect>(
    AiUpdateState(isAdmin = roleProvider() == UserRole.OWNER),
    dispatchers
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
        setState { copy(isApplying = true, showConfirmDialog = false, errorDialog = null) }
        withContext(dispatchers.default) {
            publishDocument(
                roleProvider(),
                currentState.documentId,
                currentState.editedMarkdown,
                "Applied AI-suggested update"
            )
        }
            .onSuccess {
                setState { copy(isApplying = false) }
                sendEffect(AiUpdateEffect.UpdateApplied)
            }
            .onFailure { error ->
                setState { copy(isApplying = false, errorDialog = error.toErrorDialogState("apply this update")) }
            }
    }
}
