package com.shelldocs.feature.documents.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.icons.IconChevronLeft
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.feature.documents.presentation.DocumentsIntent
import com.shelldocs.feature.documents.presentation.DocumentsState

/**
 * Full-screen document editor: raw Markdown source, live preview and the
 * attributes rail, with no explorer/list around it. [onIntent] with
 * [DocumentsIntent.CancelEditing] returns to the workspace view.
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
        ) {
            ShellGhostButton(
                text = "Back to workspace",
                icon = IconChevronLeft,
                onClick = { onIntent(DocumentsIntent.CancelEditing) },
                enabled = !state.isBusy,
            )
            Text(
                text = "Editing · ${state.selectedDocument?.title.orEmpty()}",
                style = ShellTheme.typography.sectionTitle,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f).padding(start = ShellSpacing.sm),
            )
            state.draftMessage?.let { message ->
                Text(text = message, style = ShellTheme.typography.caption, color = colors.success)
            }
            ShellGhostButton(
                text = if (state.loadingMessage == "Saving draft...") "Saving..." else "Save draft",
                onClick = { onIntent(DocumentsIntent.SaveDraft) },
                enabled = !state.isBusy,
            )
            if (state.canPublish) {
                ShellPrimaryButton(
                    text = if (state.loadingMessage == "Publishing document...") "Publishing..." else "Publish",
                    onClick = { onIntent(DocumentsIntent.Publish("Updated content")) },
                    enabled = !state.isBusy,
                )
            }
        }
        if (isWide) {
            Row(
                modifier = Modifier.weight(1f).padding(top = ShellSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(ShellSpacing.lg),
            ) {
                BasicTextField(
                    value = state.editorMarkdown,
                    onValueChange = { onIntent(DocumentsIntent.EditorChanged(it)) },
                    textStyle = ShellTheme.typography.code.copy(color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.brand),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(ShellRadius.md))
                        .background(colors.surfaceSubtle)
                        .border(
                            width = androidx.compose.ui.unit.Dp.Hairline,
                            color = colors.border,
                            shape = RoundedCornerShape(ShellRadius.md),
                        )
                        .padding(ShellSpacing.lg)
                        .verticalScroll(rememberScrollState()),
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
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = ShellSpacing.md),
                verticalArrangement = Arrangement.spacedBy(ShellSpacing.lg),
            ) {
                BasicTextField(
                    value = state.editorMarkdown,
                    onValueChange = { onIntent(DocumentsIntent.EditorChanged(it)) },
                    textStyle = ShellTheme.typography.code.copy(color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.brand),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 240.dp)
                        .clip(RoundedCornerShape(ShellRadius.md))
                        .background(colors.surfaceSubtle)
                        .border(
                            width = androidx.compose.ui.unit.Dp.Hairline,
                            color = colors.border,
                            shape = RoundedCornerShape(ShellRadius.md),
                        )
                        .padding(ShellSpacing.lg)
                        .verticalScroll(rememberScrollState()),
                )
                LiveMarkdownPreview(
                    markdown = state.editorMarkdown,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp),
                )
                state.selectedDocument?.let { document ->
                    AttributesPanel(
                        document = document,
                        modifier = Modifier.fillMaxWidth(),
                        canEdit = state.canEdit,
                        onEdit = { if (!state.isBusy) onIntent(DocumentsIntent.OpenAttributesEditor) },
                    )
                }
            }
        }
    }
}
