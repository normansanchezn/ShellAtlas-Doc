package com.shelldocs.feature.updates.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.molecules.*
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.document.ContentBlock
import com.shelldocs.feature.updates.presentation.AiUpdateEffect
import com.shelldocs.feature.updates.presentation.AiUpdateIntent
import com.shelldocs.feature.updates.presentation.AiUpdateViewModel

/**
 * Documentation review workflow: pre-analysis, then a read-only document
 * preview next to one continuous editable markdown suggestion — never an
 * editor split into per-field rows.
 */
@Composable
fun AiUpdateScreen(
    viewModel: AiUpdateViewModel,
    onApplied: () -> Unit,
    onContactOwner: (ownerName: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val colors = ShellTheme.colors

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AiUpdateEffect.UpdateApplied -> onApplied()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        val analysisStage = state.analysisStage
        if (analysisStage != null) {
            AnalysisProgress(message = analysisStage.message, modifier = Modifier.fillMaxSize())
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(ShellSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(ShellSpacing.md),
            ) {
                Column {
                    Text(
                        text = "AI Suggested Update",
                        style = ShellTheme.typography.pageTitle,
                        color = colors.textPrimary
                    )
                    Text(text = state.documentTitle, style = ShellTheme.typography.caption, color = colors.textMuted)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(ShellSpacing.md),
                ) {
                    CurrentDocumentPanel(
                        blocks = state.currentContentBlocks,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                    SuggestedUpdatePanel(
                        markdown = state.suggestedMarkdown,
                        onMarkdownChange = { viewModel.onIntent(AiUpdateIntent.EditMarkdown(it)) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    ShellPrimaryButton(
                        text = "Save Changes",
                        onClick = { viewModel.onIntent(AiUpdateIntent.SaveChanges) },
                        enabled = state.canSave,
                    )
                }
            }

            if (state.isApplying) {
                ShellLoadingOverlay(message = state.applyStage?.message ?: "Applying...")
            }
        }
    }

    if (state.showMetadataDialog) {
        MetadataReviewDialog(
            attributes = state.attributes,
            onCancel = { viewModel.onIntent(AiUpdateIntent.CancelMetadata) },
            onApply = { attributes -> viewModel.onIntent(AiUpdateIntent.ConfirmMetadata(attributes)) },
        )
    }

    if (state.showConfirmDialog) {
        ApplyUpdateConfirmDialog(
            ownerName = state.ownerName,
            isAdmin = state.isAdmin,
            onContactOwner = { onContactOwner(state.ownerName) },
            onCancel = { viewModel.onIntent(AiUpdateIntent.CancelApply) },
            onConfirm = { viewModel.onIntent(AiUpdateIntent.ConfirmApply) },
        )
    }

    state.errorDialog?.let { dialog ->
        ShellErrorDialog(state = dialog, onDismiss = { viewModel.onIntent(AiUpdateIntent.DismissError) })
    }
}

@Composable
private fun AnalysisProgress(message: String, modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ShellSpacing.sm)
        ) {
            Text(
                text = "Preparing AI Suggested Update",
                style = ShellTheme.typography.sectionTitle,
                color = colors.textPrimary
            )
            Text(text = message, style = ShellTheme.typography.body, color = colors.textMuted)
        }
    }
}

@Composable
private fun CurrentDocumentPanel(blocks: List<ContentBlock>, modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ShellRadius.md))
            .background(colors.surfaceSubtle)
            .padding(ShellSpacing.lg)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(text = "CURRENT DOCUMENT", style = ShellTheme.typography.sectionLabel, color = colors.textMuted)
        MarkdownBlocksView(blocks = blocks, modifier = Modifier.padding(top = ShellSpacing.sm))
    }
}

@Composable
private fun SuggestedUpdatePanel(
    markdown: String,
    onMarkdownChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(ShellSpacing.sm)) {
        Text(text = "AI SUGGESTED UPDATE", style = ShellTheme.typography.sectionLabel, color = colors.textMuted)
        MarkdownEditorField(
            markdown = markdown,
            onMarkdownChange = onMarkdownChange,
            modifier = Modifier.fillMaxWidth().weight(1f),
            showToolbar = true,
        )
    }
}

@Composable
private fun ApplyUpdateConfirmDialog(
    ownerName: String,
    isAdmin: Boolean,
    onContactOwner: () -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    val colors = ShellTheme.colors
    ShellDialog(
        title = "Apply Documentation Update",
        onDismiss = onCancel,
        actions = {
            ShellGhostButton(text = "Cancel", onClick = onCancel)
            ShellPrimaryButton(text = "Confirm", onClick = onConfirm)
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(ShellSpacing.md)) {
            Text(
                text = "You are about to publish this document update.\n\n" +
                        "The changes will be synchronized with all configured sources of truth and recorded in version history.\n\n" +
                        "This action can be reverted later through document version history.",
                style = ShellTheme.typography.body,
                color = colors.textSecondary,
            )
            if (isAdmin) {
                ShellCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(ShellSpacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Document Owner",
                                style = ShellTheme.typography.caption,
                                color = colors.textMuted
                            )
                            Text(text = ownerName, style = ShellTheme.typography.bodyStrong, color = colors.textPrimary)
                        }
                        ShellGhostButton(text = "Contact Owner", onClick = onContactOwner)
                    }
                }
            }
        }
    }
}
