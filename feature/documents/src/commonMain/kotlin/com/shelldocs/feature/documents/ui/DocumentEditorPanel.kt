package com.shelldocs.feature.documents.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shelldocs.core.common.testing.DemoTestTags
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.atoms.ShellIconButton
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.icons.IconChevronLeft
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
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
        } else {
            ShellIconButton(
                icon = IconChevronLeft,
                contentDescription = "Back to workspace",
                onClick = { onIntent(DocumentsIntent.CancelEditing) },
            )
        }
        Text(
            text = buildString {
                append("Editing")
                if (!isWide) {
                    append(" · ")
                    append(
                        if (state.editorStep == DocumentsEditorStep.Edit) {
                            "Source"
                        } else {
                            "Preview"
                        },
                    )
                }
                state.selectedDocument?.title?.takeIf { it.isNotBlank() }?.let {
                    append(" · ")
                    append(it)
                }
            },
            style = ShellTheme.typography.sectionTitle,
            color = ShellTheme.colors.textPrimary,
            modifier = Modifier.weight(1f).padding(start = ShellSpacing.sm),
        )
        state.draftMessage?.let { message ->
            Text(text = message, style = ShellTheme.typography.caption, color = ShellTheme.colors.success)
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

internal enum class MarkdownAction { Bold, Italic, Heading1, Heading2, Heading3, BulletList, Table }

@Composable
internal fun MarkdownEditorField(
    markdown: String,
    onMarkdownChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    showToolbar: Boolean = false,
) {
    val colors = ShellTheme.colors
    val scrollState = rememberScrollState()
    var isFocused by remember { mutableStateOf(false) }
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = markdown,
                selection = TextRange(markdown.length),
            ),
        )
    }

    LaunchedEffect(markdown) {
        if (markdown != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(
                text = markdown,
                selection = TextRange(markdown.length),
            )
        }
    }

    LaunchedEffect(textFieldValue.text, textFieldValue.selection, isFocused) {
        if (isFocused && textFieldValue.selection.end >= textFieldValue.text.length - 1) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ShellRadius.md))
            .background(colors.surfaceSubtle)
            .border(
                width = Dp.Hairline,
                color = colors.border,
                shape = RoundedCornerShape(ShellRadius.md),
            ),
    ) {
        if (showToolbar) {
            MarkdownToolbar(
                onAction = { action ->
                    val updated = applyMarkdownAction(action, textFieldValue)
                    textFieldValue = updated
                    onMarkdownChange(updated.text)
                },
            )
        }
        BasicTextField(
            value = textFieldValue,
            onValueChange = { updated ->
                textFieldValue = updated
                onMarkdownChange(updated.text)
            },
            textStyle = ShellTheme.typography.code.copy(color = colors.textPrimary),
            cursorBrush = SolidColor(colors.brand),
            modifier = fieldModifier
                .fillMaxWidth()
                .weight(1f)
                .onFocusChanged { isFocused = it.isFocused }
                .padding(ShellSpacing.lg)
                .verticalScroll(scrollState),
        )
    }
}

/** Pinned row of formatting shortcuts above the editor; stays put while the field scrolls. */
@Composable
private fun MarkdownToolbar(onAction: (MarkdownAction) -> Unit) {
    val colors = ShellTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .border(
                width = Dp.Hairline,
                color = colors.border,
            )
            .padding(horizontal = ShellSpacing.sm, vertical = ShellSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(ShellSpacing.xs),
    ) {
        MarkdownToolbarButton("B", "Bold") { onAction(MarkdownAction.Bold) }
        MarkdownToolbarButton("I", "Italic") { onAction(MarkdownAction.Italic) }
        MarkdownToolbarButton("H1", "Heading 1") { onAction(MarkdownAction.Heading1) }
        MarkdownToolbarButton("H2", "Heading 2") { onAction(MarkdownAction.Heading2) }
        MarkdownToolbarButton("H3", "Heading 3") { onAction(MarkdownAction.Heading3) }
        MarkdownToolbarButton("•", "Bullet list") { onAction(MarkdownAction.BulletList) }
        MarkdownToolbarButton("⊞", "Table") { onAction(MarkdownAction.Table) }
    }
}

@Composable
private fun MarkdownToolbarButton(label: String, contentDescription: String, onClick: () -> Unit) {
    val colors = ShellTheme.colors
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 32.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(ShellRadius.sm))
            .clickable(onClick = onClick)
            .padding(horizontal = ShellSpacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = ShellTheme.typography.label,
            color = colors.textSecondary,
        )
    }
}

private fun applyMarkdownAction(action: MarkdownAction, value: TextFieldValue): TextFieldValue = when (action) {
    MarkdownAction.Bold -> wrapSelection(value, "**", "**", "bold text")
    MarkdownAction.Italic -> wrapSelection(value, "_", "_", "italic text")
    MarkdownAction.Heading1 -> prefixLine(value, "# ")
    MarkdownAction.Heading2 -> prefixLine(value, "## ")
    MarkdownAction.Heading3 -> prefixLine(value, "### ")
    MarkdownAction.BulletList -> prefixLine(value, "- ")
    MarkdownAction.Table -> insertBlock(
        value,
        "\n| Column 1 | Column 2 |\n| --- | --- |\n| Cell | Cell |\n",
    )
}

private fun wrapSelection(value: TextFieldValue, prefix: String, suffix: String, placeholder: String): TextFieldValue {
    val text = value.text
    val start = value.selection.min
    val end = value.selection.max
    return if (start == end) {
        val newText = text.substring(0, start) + prefix + placeholder + suffix + text.substring(end)
        TextFieldValue(newText, TextRange(start + prefix.length, start + prefix.length + placeholder.length))
    } else {
        val selected = text.substring(start, end)
        val newText = text.substring(0, start) + prefix + selected + suffix + text.substring(end)
        TextFieldValue(newText, TextRange(start + prefix.length, end + prefix.length))
    }
}

private fun prefixLine(value: TextFieldValue, marker: String): TextFieldValue {
    val text = value.text
    val cursor = value.selection.min
    val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
    val newText = text.substring(0, lineStart) + marker + text.substring(lineStart)
    val newCursor = value.selection.max + marker.length
    return TextFieldValue(newText, TextRange(newCursor))
}

private fun insertBlock(value: TextFieldValue, block: String): TextFieldValue {
    val text = value.text
    val start = value.selection.min
    val end = value.selection.max
    val newText = text.substring(0, start) + block + text.substring(end)
    return TextFieldValue(newText, TextRange(start + block.length))
}
