package com.shelldocs.feature.updates.presentation

import com.shelldocs.core.common.coroutines.DispatcherProvider
import com.shelldocs.core.common.error.toErrorDialogState
import com.shelldocs.core.common.mvi.MviViewModel
import com.shelldocs.core.common.result.onFailure
import com.shelldocs.core.common.result.onSuccess
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.entity.document.Area
import com.shelldocs.core.domain.entity.document.DocumentClassificationResult
import com.shelldocs.core.domain.entity.document.MetadataAssignment
import com.shelldocs.core.domain.entity.document.MetadataAttribute
import com.shelldocs.core.domain.entity.updates.PendingUpdate
import com.shelldocs.core.domain.entity.updates.RiskLevel
import com.shelldocs.core.domain.usecase.classification.ApplyMetadataAssignmentsUseCase
import com.shelldocs.core.domain.usecase.classification.AssignMetadataUseCase
import com.shelldocs.core.domain.usecase.classification.GetMetadataIssuesUseCase
import com.shelldocs.core.domain.usecase.updates.GetHealthyDocumentsUseCase
import com.shelldocs.core.domain.usecase.updates.ScanForUpdatesUseCase
import com.shelldocs.core.domain.usecase.updates.SetManualRiskLevelUseCase
import kotlinx.coroutines.withContext

class UpdatesViewModel(
    private val scanForUpdates: ScanForUpdatesUseCase,
    private val getMetadataIssues: GetMetadataIssuesUseCase,
    private val getHealthyDocuments: GetHealthyDocumentsUseCase,
    private val applyMetadataAssignments: ApplyMetadataAssignmentsUseCase,
    private val assignMetadata: AssignMetadataUseCase,
    private val setManualRiskLevel: SetManualRiskLevelUseCase,
    private val currentUserRole: UserRole,
    private val visibleArea: Area?,
    private val canUpdateDocuments: Boolean,
    dispatchers: DispatcherProvider,
) : MviViewModel<UpdatesIntent, UpdatesState, UpdatesEffect>(
    UpdatesState(isAdmin = currentUserRole == UserRole.OWNER, canUpdateDocuments = canUpdateDocuments),
    dispatchers,
) {

    private val isAdmin get() = currentUserRole == UserRole.OWNER

    override suspend fun handleIntent(intent: UpdatesIntent) {
        when (intent) {
            UpdatesIntent.Initialize -> scan()
            UpdatesIntent.DismissError -> setState { copy(errorDialog = null) }
            is UpdatesIntent.ToggleRiskFilter ->
                setState { copy(riskFilter = if (riskFilter == intent.risk) null else intent.risk) }
            is UpdatesIntent.SelectTab -> selectTab(intent.tab)
            is UpdatesIntent.AssignMetadata -> assign(intent.documentId, intent.attribute, intent.value)
            is UpdatesIntent.ApplyMetadataAssignments -> applyAssignments(intent.documentId, intent.assignments)
            is UpdatesIntent.SetManualRisk -> setManualRisk(intent.documentId, intent.risk)
            is UpdatesIntent.OpenUpdate -> sendEffect(UpdatesEffect.OpenAiUpdate(intent.documentId))
            is UpdatesIntent.OpenDocument -> sendEffect(UpdatesEffect.OpenDocument(intent.documentId))
        }
    }

    private fun visibleTo(area: Area?): Boolean =
        isAdmin || area == visibleArea

    private fun List<PendingUpdate>.visiblePendingUpdates() = filter { visibleTo(it.area) }.distinctBy { it.documentId }

    private fun List<DocumentClassificationResult>.visibleMetadataIssues() =
        filter { visibleTo(it.area) }.distinctBy { it.documentId }

    private suspend fun scan() {
        setState { copy(isScanning = true, errorDialog = null) }
        withContext(dispatchers.default) {
            scanForUpdates()
        }
            .onSuccess { updates -> setState { copy(isScanning = false, updates = updates.visiblePendingUpdates()) } }
            .onFailure { error ->
                setState { copy(isScanning = false, errorDialog = error.toErrorDialogState("scan for updates")) }
            }
        loadMetadataIssues()
        loadHealthyDocuments()
    }

    private suspend fun selectTab(tab: DocumentationHealthTab) {
        setState { copy(selectedTab = tab) }
        when {
            tab == DocumentationHealthTab.METADATA_ISSUES && currentState.metadataIssues.isEmpty() -> loadMetadataIssues()
            tab == DocumentationHealthTab.HEALTHY && currentState.healthyDocuments.isEmpty() -> loadHealthyDocuments()
        }
    }

    private suspend fun loadMetadataIssues() {
        setState { copy(isLoadingMetadataIssues = true) }
        withContext(dispatchers.default) {
            getMetadataIssues()
        }
            .onSuccess { issues -> setState { copy(isLoadingMetadataIssues = false, metadataIssues = issues.visibleMetadataIssues()) } }
            .onFailure { error ->
                setState { copy(isLoadingMetadataIssues = false, errorDialog = error.toErrorDialogState("load metadata issues")) }
            }
    }

    private suspend fun loadHealthyDocuments() {
        setState { copy(isLoadingHealthyDocuments = true) }
        withContext(dispatchers.default) {
            getHealthyDocuments()
        }
            .onSuccess { docs ->
                setState {
                    copy(
                        isLoadingHealthyDocuments = false,
                        healthyDocuments = docs.visiblePendingUpdates()
                    )
                }
            }
            .onFailure { error ->
                setState {
                    copy(
                        isLoadingHealthyDocuments = false,
                        errorDialog = error.toErrorDialogState("load healthy documents")
                    )
                }
            }
    }

    private suspend fun assign(documentId: String, attribute: MetadataAttribute, value: String) {
        withContext(dispatchers.default) {
            assignMetadata(documentId, attribute, value)
        }
            .onSuccess {
                sendEffect(UpdatesEffect.MetadataUpdated(documentId))
                loadMetadataIssues()
                loadHealthyDocuments()
            }
            .onFailure { error ->
                setState { copy(errorDialog = error.toErrorDialogState("assign metadata")) }
            }
    }

    private suspend fun applyAssignments(documentId: String, assignments: List<MetadataAssignment>) {
        withContext(dispatchers.default) {
            applyMetadataAssignments(documentId, assignments)
        }
            .onSuccess {
                sendEffect(UpdatesEffect.MetadataUpdated(documentId))
                loadMetadataIssues()
                loadHealthyDocuments()
                sendEffect(UpdatesEffect.OpenAiUpdate(documentId))
            }
            .onFailure { error ->
                setState { copy(errorDialog = error.toErrorDialogState("apply metadata changes")) }
            }
    }

    private suspend fun setManualRisk(documentId: String, risk: RiskLevel?) {
        withContext(dispatchers.default) {
            setManualRiskLevel(currentUserRole, documentId, risk)
        }
            .onSuccess { updated ->
                setState { copy(updates = updates.map { if (it.documentId == updated.documentId) updated else it }) }
            }
            .onFailure { error ->
                setState { copy(errorDialog = error.toErrorDialogState("set risk classification")) }
            }
    }
}
