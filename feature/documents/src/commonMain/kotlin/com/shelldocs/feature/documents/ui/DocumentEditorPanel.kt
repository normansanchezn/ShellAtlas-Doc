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
import com.shelldocs.core.designsystem.icons.IconChevronLeft
import com.shelldocs.core.designsystem.molecules.MarkdownEditorField
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.feature.documents.presentation.DocumentsEditorStep
import com.shelldocs.feature.documents.presentation.DocumentsIntent
import com.shelldocs.feature.documents.presentation.DocumentsState

/**
 * Full-screen document editor. Desktop keeps the existing multi-pane experience.
 * Mobile uses a stepped flow: edit source -> add attributes -> preview -> publish.
 */
@Composable
fun DocumentEditorPanel(
    state: DocumentsState,
    onIntent: (DocumentsIntent) -> Unit,
    modifier: Modifier = Modifier,
    isWide: Boolean = true,
) {
    val colors = ShellTheme.colors
    Column(modifier = modifier.background(colors.background).padding(ShellSpacing.lg)) {
        DocumentEditorHeader(
            state = state,
            onIntent = onIntent,
            isWide = isWide,
        )
        if (isWide) {
            DesktopDocumentEditor(
                state = state,
                onIntent = onIntent,
                modifier = Modifier.weight(1f).padding(top = ShellSpacing.md),
            )
        } else {
            MobileDocumentEditor(
                state = state,
                onIntent = onIntent,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = ShellSpacing.md)
                    .imePadding(),
            )
        }
    }
}

@Composable
private fun DocumentEditorHeader(
    state: DocumentsState,
    onIntent: (DocumentsIntent) -> Unit,
    isWide: Boolean,
) {
    val titleText = buildString {
        append("Editing")
        if (!isWide) {
            append(" · ")
            append(if (state.editorStep == DocumentsEditorStep.Edit) "Source" else "Preview")
        }
        state.selectedDocument?.title?.takeIf { it.isNotBlank() }?.let {
            append(" · ")
            append(it)
        }
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
        ) {
            if (isWide) {
                ShellGhostButton(
                    text = "Back to workspace",
                    icon = IconChevronLeft,
                    onClick = { onIntent(DocumentsIntent.CancelEditing) },
                    enabled = !state.isBusy,
                )
                Text(
                    text = titleText,
                    style = ShellTheme.typography.sectionTitle,
                    color = ShellTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(start = ShellSpacing.sm),
                )
            } else {
                ShellIconButton(
                    icon = IconChevronLeft,
                    contentDescription = "Back to workspace",
                    onClick = { onIntent(DocumentsIntent.CancelEditing) },
                )
                // Title moves to its own row below — a long doc title here would
                // wrap across several lines and push the back/save controls apart.
                Spacer(modifier = Modifier.weight(1f))
            }
            state.draftMessage?.let { message ->
                Text(
                    text = message,
                    style = ShellTheme.typography.caption,
                    color = ShellTheme.colors.success,
                    maxLines = 1,
                )
            }
            ShellGhostButton(
                text = if (state.loadingMessage == "Saving draft...") "Saving..." else "Save draft",
                onClick = { onIntent(DocumentsIntent.SaveDraft) },
                enabled = !state.isBusy,
                modifier = Modifier.testTag(DemoTestTags.DocumentsSaveDraft),
            )
            if (isWide && state.canPublish) {
                ShellPrimaryButton(
                    text = if (state.loadingMessage == "Publishing document...") "Publishing..." else "Publish",
                    onClick = { onIntent(DocumentsIntent.Publish("Updated content")) },
                    enabled = !state.isBusy,
                    modifier = Modifier.testTag(DemoTestTags.DocumentsPublish),
                )
            }
        }
        if (!isWide) {
            Text(
                text = titleText,
                style = ShellTheme.typography.sectionTitle,
                color = ShellTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(top = ShellSpacing.xs),
            )
        }
    }
}

@Composable
private fun DesktopDocumentEditor(
    state: DocumentsState,
    onIntent: (DocumentsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ShellSpacing.lg),
    ) {
        MarkdownEditorField(
            markdown = state.editorMarkdown,
            onMarkdownChange = { onIntent(DocumentsIntent.EditorChanged(it)) },
            modifier = Modifier.weight(1f).fillMaxSize(),
            fieldModifier = Modifier.testTag(DemoTestTags.DocumentsEditorMarkdown),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            LiveMarkdownPreview(markdown = state.editorMarkdown)
        }
        state.selectedDocument?.let { document ->
            AttributesPanel(
                document = document,
                modifier = Modifier.width(260.dp).fillMaxHeight(),
                canEdit = state.canEdit,
                onEdit = { if (!state.isBusy) onIntent(DocumentsIntent.OpenAttributesEditor) },
            )
        }
    }
}

@Composable
private fun MobileDocumentEditor(
    state: DocumentsState,
    onIntent: (DocumentsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state.editorStep) {
        DocumentsEditorStep.Edit -> MobileEditorStep(
            state = state,
            onIntent = onIntent,
            modifier = modifier,
        )
        DocumentsEditorStep.Preview -> MobilePreviewStep(
            state = state,
            onIntent = onIntent,
            modifier = modifier,
        )
    }
}

@Composable
private fun MobileEditorStep(
    state: DocumentsState,
    onIntent: (DocumentsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ShellSpacing.md),
    ) {
        Text(
            text = "Finish the content first. Preview stays hidden until you continue from the end of the document.",
            style = ShellTheme.typography.caption,
            color = ShellTheme.colors.textMuted,
        )
        MarkdownEditorField(
            markdown = state.editorMarkdown,
            onMarkdownChange = { onIntent(DocumentsIntent.EditorChanged(it)) },
            modifier = Modifier.fillMaxWidth().weight(1f),
            fieldModifier = Modifier.testTag(DemoTestTags.DocumentsEditorMarkdown),
            showToolbar = true,
        )
        ShellPrimaryButton(
            text = "Continue to preview",
            onClick = { onIntent(DocumentsIntent.ContinueToPreview) },
            enabled = !state.isBusy,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun MobilePreviewStep(
    state: DocumentsState,
    onIntent: (DocumentsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val document = state.selectedDocument
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(ShellSpacing.lg),
    ) {
        document?.let {
            AttributesPanel(
                document = it,
                modifier = Modifier.fillMaxWidth(),
                canEdit = state.canEdit,
                onEdit = { if (!state.isBusy) onIntent(DocumentsIntent.OpenAttributesEditor) },
            )
        }
        LiveMarkdownPreview(
            markdown = state.editorMarkdown,
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
                onClick = { onIntent(DocumentsIntent.BackToEditor) },
                enabled = !state.isBusy,
                modifier = Modifier.weight(1f),
            )
            if (state.canPublish) {
                ShellPrimaryButton(
                    text = if (state.loadingMessage == "Publishing document...") "Publishing..." else "Publish",
                    onClick = { onIntent(DocumentsIntent.Publish("Updated content")) },
                    enabled = !state.isBusy,
                    modifier = Modifier.weight(1f).testTag(DemoTestTags.DocumentsPublish),
                )
            }
        }
    }
}
