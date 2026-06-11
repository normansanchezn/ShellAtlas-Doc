package com.shelldocs.feature.documents.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.feature.documents.presentation.DocumentsIntent
import com.shelldocs.feature.documents.presentation.DocumentsState

/** Split Markdown editor: raw source left, live preview right. */
@Composable
fun DocumentEditorPanel(
    state: DocumentsState,
    onIntent: (DocumentsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Column(modifier = modifier.padding(ShellSpacing.lg)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
        ) {
            Text(
                text = "Editing · ${state.selectedDocument?.title.orEmpty()}",
                style = ShellTheme.typography.sectionTitle,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            state.draftMessage?.let { message ->
                Text(text = message, style = ShellTheme.typography.caption, color = colors.success)
            }
            ShellGhostButton(text = "Cancel", onClick = { onIntent(DocumentsIntent.CancelEditing) })
            ShellGhostButton(text = "Save draft", onClick = { onIntent(DocumentsIntent.SaveDraft) })
            if (state.canPublish) {
                ShellPrimaryButton(
                    text = "Publish",
                    onClick = { onIntent(DocumentsIntent.Publish("Updated content")) },
                )
            }
        }
        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage.orEmpty(),
                style = ShellTheme.typography.caption,
                color = colors.danger,
                modifier = Modifier.padding(top = ShellSpacing.xs),
            )
        }
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
        }
    }
}
