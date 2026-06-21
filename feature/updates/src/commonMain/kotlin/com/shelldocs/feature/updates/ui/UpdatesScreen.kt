package com.shelldocs.feature.updates.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.shelldocs.core.common.testing.DemoTestTags
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.icons.IconRefresh
import com.shelldocs.core.designsystem.molecules.ShellErrorDialog
import com.shelldocs.core.designsystem.molecules.ShellLoadingOverlay
import com.shelldocs.core.designsystem.molecules.ShellScreenToolbar
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.document.DocumentClassificationResult
import com.shelldocs.core.domain.entity.document.MetadataAssignment
import com.shelldocs.feature.updates.UpdatesStringRes
import com.shelldocs.feature.updates.presentation.DocumentationHealthTab
import com.shelldocs.feature.updates.presentation.UpdatesEffect
import com.shelldocs.feature.updates.presentation.UpdatesIntent
import com.shelldocs.feature.updates.presentation.UpdatesViewModel

/** Documentation Health: risk summary + triage table, with a Metadata Issues section. */
@Composable
fun UpdatesScreen(
    viewModel: UpdatesViewModel,
    isWide: Boolean,
    onOpenAiUpdate: (documentId: String) -> Unit,
    onOpenDocument: (documentId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val colors = ShellTheme.colors
    var editTarget by remember { mutableStateOf<DocumentClassificationResult?>(null) }
    var pendingConfirmation by remember {
        mutableStateOf<Pair<DocumentClassificationResult, List<MetadataAssignment>>?>(null)
    }

    LaunchedEffect(viewModel) { viewModel.onIntent(UpdatesIntent.Initialize) }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is UpdatesEffect.OpenAiUpdate -> onOpenAiUpdate(effect.documentId)
                is UpdatesEffect.OpenDocument -> onOpenDocument(effect.documentId)
                else -> Unit
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ShellSpacing.lg, vertical = ShellSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ShellSpacing.lg),
        ) {
            ShellScreenToolbar(
                title = UpdatesStringRes.PAGE_TITLE,
                subtitle = "${state.updates.size} ${UpdatesStringRes.PAGE_SUBTITLE_SUFFIX}",
                trailingContent = {
                    ShellGhostButton(
                        text = if (state.isScanning) UpdatesStringRes.SCANNING else UpdatesStringRes.SCAN_NOW,
                        icon = IconRefresh,
                        onClick = { viewModel.onIntent(UpdatesIntent.ScanNow) },
                        enabled = !state.isScanning && !state.isLoading,
                        modifier = Modifier.testTag(DemoTestTags.UpdatesScan),
                    )
                },
            )

            DocumentationHealthTabRow(
                selectedTab = state.selectedTab,
                metadataIssueCount = state.metadataIssuesRequiringAttention,
                onSelect = { tab -> viewModel.onIntent(UpdatesIntent.SelectTab(tab)) },
            )

            when (state.selectedTab) {
                DocumentationHealthTab.HEALTH -> {
                    RiskSummaryRow(state = state, onIntent = viewModel::onIntent, isWide = isWide)
                    UpdatesTable(
                        state = state,
                        isWide = isWide,
                        onSetRisk = { documentId, risk -> viewModel.onIntent(UpdatesIntent.SetManualRisk(documentId, risk)) },
                        onOpenUpdate = { documentId -> viewModel.onIntent(UpdatesIntent.OpenUpdate(documentId)) },
                    )
                }
                DocumentationHealthTab.METADATA_ISSUES -> {
                    MetadataIssuesTable(
                        issues = state.metadataIssues,
                        isAdmin = state.isAdmin,
                        isWide = isWide,
                        onEditMetadata = { issue -> editTarget = issue },
                    )
                }
                DocumentationHealthTab.HEALTHY -> {
                    HealthyDocumentsTable(
                        documents = state.healthyDocuments,
                        isWide = isWide,
                        onOpenDocument = { documentId -> viewModel.onIntent(UpdatesIntent.OpenDocument(documentId)) },
                    )
                }
            }
        }

        when {
            state.isLoading -> ShellLoadingOverlay(message = UpdatesStringRes.LOADING_PENDING_UPDATES)
            state.isScanning -> ShellLoadingOverlay(message = UpdatesStringRes.LOADING_DOCUMENTATION_HEALTH)
            state.isLoadingMetadataIssues && state.selectedTab == DocumentationHealthTab.METADATA_ISSUES ->
                ShellLoadingOverlay(message = UpdatesStringRes.LOADING_CLASSIFYING_DOCUMENTS)
            state.isLoadingHealthyDocuments && state.selectedTab == DocumentationHealthTab.HEALTHY ->
                ShellLoadingOverlay(message = UpdatesStringRes.LOADING_HEALTHY_DOCUMENTS)
        }
    }

    state.errorDialog?.let { dialog ->
        ShellErrorDialog(
            state = dialog,
            onDismiss = { viewModel.onIntent(UpdatesIntent.DismissError) },
        )
    }

    editTarget?.let { issue ->
        EditMetadataDialog(
            issue = issue,
            onDismiss = { editTarget = null },
            onAccept = { _, assignments ->
                pendingConfirmation = issue to assignments
                editTarget = null
            },
        )
    }

    pendingConfirmation?.let { (issue, assignments) ->
        ConfirmMetadataUpdateDialog(
            issue = issue,
            assignments = assignments,
            onDismiss = { pendingConfirmation = null },
            onConfirm = {
                viewModel.onIntent(UpdatesIntent.ApplyMetadataAssignments(issue.documentId, assignments))
                pendingConfirmation = null
            },
        )
    }
}
