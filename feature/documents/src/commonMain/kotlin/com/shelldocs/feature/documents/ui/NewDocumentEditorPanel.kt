package com.shelldocs.feature.documents.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shelldocs.core.common.testing.DemoTestTags
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.atoms.ShellIconButton
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.atoms.ShellTextField
import com.shelldocs.core.designsystem.icons.IconChevronLeft
import com.shelldocs.core.designsystem.molecules.MarkdownEditorField
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.feature.documents.presentation.AttributesDraft
import com.shelldocs.feature.documents.presentation.DocumentsEditorStep
import com.shelldocs.feature.documents.presentation.DocumentsIntent
import com.shelldocs.feature.documents.presentation.DocumentsState

@Composable
fun NewDocumentEditorPanel(
    state: DocumentsState,
    onIntent: (DocumentsIntent) -> Unit,
    isWide: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Column(modifier = modifier.background(colors.background).padding(ShellSpacing.lg)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
        ) {
            if (isWide) {
                ShellGhostButton(
                    text = "Back to workspace",
                    icon = IconChevronLeft,
                    onClick = { onIntent(DocumentsIntent.CancelNewDocument) },
                    enabled = !state.isBusy,
                )
            } else {
                ShellIconButton(
                    icon = IconChevronLeft,
                    contentDescription = "Back to workspace",
                    onClick = { onIntent(DocumentsIntent.CancelNewDocument) },
                )
            }
            Text(
                text = buildString {
                    append("New document")
                    if (!isWide) {
                        append(" · ")
                        append(if (state.newDocumentStep == DocumentsEditorStep.Edit) "Source" else "Preview")
                    }
                },
                style = ShellTheme.typography.sectionTitle,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(start = ShellSpacing.sm),
            )
            if (isWide) {
                ShellPrimaryButton(
                    text = if (state.loadingMessage == "Creating document...") "Creating..." else "Create document",
                    onClick = { onIntent(DocumentsIntent.SubmitNewDocument) },
                    enabled = !state.isBusy,
                    modifier = Modifier.testTag(DemoTestTags.DocumentsCreate),
                )
            }
        }

        if (isWide) {
            WideNewDocumentEditor(
                state = state,
                onIntent = onIntent,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = ShellSpacing.md),
            )
        } else {
            MobileNewDocumentEditor(
                state = state,
                onIntent = onIntent,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = ShellSpacing.md)
                    .imePadding(),
            )
        }
    }
}

@Composable
private fun WideNewDocumentEditor(
    state: DocumentsState,
    onIntent: (DocumentsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ShellSpacing.md),
    ) {
        ShellTextField(
            value = state.newDocumentTitle,
            onValueChange = { onIntent(DocumentsIntent.NewDocumentTitleChanged(it)) },
            placeholder = "Document title",
            modifier = Modifier.fillMaxWidth().testTag(DemoTestTags.DocumentsNewTitle),
        )
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(ShellSpacing.lg),
        ) {
            MarkdownEditorField(
                markdown = state.newDocumentMarkdown,
                onMarkdownChange = { onIntent(DocumentsIntent.NewDocumentMarkdownChanged(it)) },
                modifier = Modifier.weight(1f).fillMaxSize(),
                fieldModifier = Modifier.testTag(DemoTestTags.DocumentsNewMarkdown),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "Preview",
                    style = ShellTheme.typography.label,
                    color = ShellTheme.colors.textMuted,
                    modifier = Modifier.padding(bottom = ShellSpacing.sm),
                )
                LiveMarkdownPreview(markdown = state.newDocumentMarkdown)
            }
        }
    }
}

@Composable
private fun MobileNewDocumentEditor(
    state: DocumentsState,
    onIntent: (DocumentsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state.newDocumentStep) {
        DocumentsEditorStep.Edit -> MobileNewDocumentEditStep(state = state, onIntent = onIntent, modifier = modifier)
        DocumentsEditorStep.Preview -> MobileNewDocumentPreviewStep(state = state, onIntent = onIntent, modifier = modifier)
    }
}

@Composable
private fun MobileNewDocumentEditStep(
    state: DocumentsState,
    onIntent: (DocumentsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ShellSpacing.md),
    ) {
        ShellTextField(
            value = state.newDocumentTitle,
            onValueChange = { onIntent(DocumentsIntent.NewDocumentTitleChanged(it)) },
            placeholder = "Document title",
            modifier = Modifier.fillMaxWidth().testTag(DemoTestTags.DocumentsNewTitle),
        )
        Text(
            text = "Write the draft first. Preview stays hidden until you continue and complete the document attributes.",
            style = ShellTheme.typography.caption,
            color = ShellTheme.colors.textMuted,
        )
        MarkdownEditorField(
            markdown = state.newDocumentMarkdown,
            onMarkdownChange = { onIntent(DocumentsIntent.NewDocumentMarkdownChanged(it)) },
            modifier = Modifier.fillMaxWidth().weight(1f),
            fieldModifier = Modifier.testTag(DemoTestTags.DocumentsNewMarkdown),
            showToolbar = true,
        )
        ShellPrimaryButton(
            text = "Continue to preview",
            onClick = { onIntent(DocumentsIntent.ContinueNewDocumentToPreview) },
            enabled = !state.isBusy,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun MobileNewDocumentPreviewStep(
    state: DocumentsState,
    onIntent: (DocumentsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(ShellSpacing.lg),
    ) {
        if (state.newDocumentTitle.isNotBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(ShellSpacing.xs)) {
                Text(
                    text = "Title",
                    style = ShellTheme.typography.caption,
                    color = ShellTheme.colors.textMuted,
                )
                Text(
                    text = state.newDocumentTitle,
                    style = ShellTheme.typography.bodyStrong,
                    color = ShellTheme.colors.textPrimary,
                )
            }
        }
        DraftAttributesPanel(
            draft = state.attributesDraft,
            onEdit = { onIntent(DocumentsIntent.OpenNewDocumentAttributesEditor) },
        )
        LiveMarkdownPreview(
            markdown = state.newDocumentMarkdown.ifBlank { "# ${state.newDocumentTitle}" },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 260.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
        ) {
            ShellGhostButton(
                text = "Back",
                onClick = { onIntent(DocumentsIntent.BackToNewDocumentEditor) },
                enabled = !state.isBusy,
                modifier = Modifier.weight(1f),
            )
            ShellPrimaryButton(
                text = if (state.loadingMessage == "Creating document...") "Creating..." else "Create document",
                onClick = { onIntent(DocumentsIntent.SubmitNewDocument) },
                enabled = !state.isBusy,
                modifier = Modifier.weight(1f).testTag(DemoTestTags.DocumentsCreate),
            )
        }
    }
}

@Composable
private fun DraftAttributesPanel(
    draft: AttributesDraft,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Attributes",
                style = ShellTheme.typography.sectionTitle,
                color = ShellTheme.colors.textPrimary,
            )
            ShellGhostButton(text = "Edit", onClick = onEdit)
        }
        DraftAttributeRow("Owner", draft.owner)
        DraftAttributeRow("Module", draft.module)
        DraftAttributeRow("Team", draft.team)
        DraftAttributeRow("Platform", draft.platform)
        DraftAttributeRow("Tags", draft.tagsText)
    }
}

@Composable
private fun DraftAttributeRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = ShellTheme.typography.caption,
            color = ShellTheme.colors.textMuted,
        )
        Text(
            text = value.ifBlank { "—" },
            style = ShellTheme.typography.caption,
            color = ShellTheme.colors.textPrimary,
        )
    }
}
