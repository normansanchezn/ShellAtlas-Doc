package com.shelldocs.feature.updates.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.atoms.ShellTextField
import com.shelldocs.core.designsystem.icons.IconX
import com.shelldocs.core.designsystem.molecules.ShellDialog
import com.shelldocs.core.designsystem.molecules.ShellErrorDialog
import com.shelldocs.core.designsystem.molecules.ShellLoadingOverlay
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.document.LineOrigin
import com.shelldocs.core.domain.entity.document.SuggestionLine
import com.shelldocs.feature.updates.presentation.AiUpdateEffect
import com.shelldocs.feature.updates.presentation.AiUpdateIntent
import com.shelldocs.feature.updates.presentation.AiUpdateViewModel

private val AiHighlightDark = Color(0xFFEAB308) // Yellow 500
private val AiHighlightLight = Color(0xFFD97706) // Amber 600

/** AI Assisted Update: read-only current document next to an editable AI suggestion. */
@Composable
fun AiUpdateScreen(
    viewModel: AiUpdateViewModel,
    onApplied: () -> Unit,
    onContactOwner: (ownerName: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val colors = ShellTheme.colors
    val highlight = if (colors.isDark) AiHighlightDark else AiHighlightLight

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AiUpdateEffect.UpdateApplied -> onApplied()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(ShellSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(ShellSpacing.md)
        ) {
            Column {
                Text(text = "AI Suggested Update", style = ShellTheme.typography.pageTitle, color = colors.textPrimary)
                Text(text = state.documentTitle, style = ShellTheme.typography.caption, color = colors.textMuted)
            }

            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(ShellSpacing.md),
            ) {
                CurrentDocumentPanel(markdown = state.currentMarkdown, modifier = Modifier.weight(1f).fillMaxHeight())
                SuggestedUpdatePanel(
                    lines = state.suggestedLines,
                    highlight = highlight,
                    onEditLine = { index, text -> viewModel.onIntent(AiUpdateIntent.EditLine(index, text)) },
                    onRemoveLine = { index -> viewModel.onIntent(AiUpdateIntent.RemoveLine(index)) },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                ShellPrimaryButton(
                    text = if (state.isApplying) "Saving Document..." else "Apply Update",
                    onClick = { viewModel.onIntent(AiUpdateIntent.RequestApply) },
                    enabled = state.canApply,
                )
            }
        }

        if (state.isLoading) ShellLoadingOverlay(message = "Generating AI Suggestion...")
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
private fun CurrentDocumentPanel(markdown: String, modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ShellRadius.md))
            .background(colors.surfaceSubtle)
            .padding(ShellSpacing.lg)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(text = "CURRENT DOCUMENT", style = ShellTheme.typography.sectionLabel, color = colors.textMuted)
        Text(
            text = markdown,
            style = ShellTheme.typography.body,
            color = colors.textSecondary,
            modifier = Modifier.padding(top = ShellSpacing.sm),
        )
    }
}

@Composable
private fun SuggestedUpdatePanel(
    lines: List<SuggestionLine>,
    highlight: Color,
    onEditLine: (index: Int, text: String) -> Unit,
    onRemoveLine: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ShellRadius.md))
            .background(colors.surface)
            .padding(ShellSpacing.lg)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(ShellSpacing.xs),
    ) {
        Text(text = "AI SUGGESTED UPDATE", style = ShellTheme.typography.sectionLabel, color = colors.textMuted)
        lines.forEachIndexed { index, line ->
            SuggestionLineRow(
                text = line.text,
                isHighlighted = line.origin == LineOrigin.AI_SUGGESTED,
                highlight = highlight,
                onTextChange = { onEditLine(index, it) },
                onRemove = { onRemoveLine(index) },
            )
        }
    }
}

@Composable
private fun SuggestionLineRow(
    text: String,
    isHighlighted: Boolean,
    highlight: Color,
    onTextChange: (String) -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ShellRadius.sm))
            .background(if (isHighlighted) highlight.copy(alpha = 0.18f) else Color.Transparent),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShellTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
        )
        if (isHighlighted) {
            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                Icon(imageVector = IconX, contentDescription = "Remove suggestion", tint = ShellTheme.colors.textMuted)
            }
        }
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
        title = "Apply Documentation Update?",
        onDismiss = onCancel,
        actions = {
            ShellGhostButton(text = "Cancel", onClick = onCancel)
            ShellPrimaryButton(text = "Confirm", onClick = onConfirm)
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(ShellSpacing.sm)) {
            Text(
                text = "After applying this update, all modifications will be recorded in the document version history. " +
                        "You can review or revert these changes at any time.",
                style = ShellTheme.typography.body,
                color = colors.textSecondary,
            )
            if (isAdmin) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Document Owner", style = ShellTheme.typography.caption, color = colors.textMuted)
                        Text(text = ownerName, style = ShellTheme.typography.bodyStrong, color = colors.textPrimary)
                    }
                    ShellGhostButton(text = "Contact Owner", onClick = onContactOwner)
                }
            }
        }
    }
}
