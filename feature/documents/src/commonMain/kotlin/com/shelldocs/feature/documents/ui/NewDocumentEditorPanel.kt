package com.shelldocs.feature.documents.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.shelldocs.core.common.testing.DemoTestTags
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.atoms.ShellTextField
import com.shelldocs.core.designsystem.icons.IconChevronLeft
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
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
            ShellGhostButton(
                text = "Back to workspace",
                icon = IconChevronLeft,
                onClick = { onIntent(DocumentsIntent.CancelNewDocument) },
                enabled = !state.isBusy,
            )
            Text(
                text = "New document",
                style = ShellTheme.typography.sectionTitle,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f).padding(start = ShellSpacing.sm),
            )
            ShellPrimaryButton(
                text = if (state.loadingMessage == "Creating document...") "Creating..." else "Create document",
                onClick = { onIntent(DocumentsIntent.SubmitNewDocument) },
                enabled = !state.isBusy,
                modifier = Modifier.testTag(DemoTestTags.DocumentsCreate),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = ShellSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ShellSpacing.md),
        ) {
            ShellTextField(
                value = state.newDocumentTitle,
                onValueChange = { onIntent(DocumentsIntent.NewDocumentTitleChanged(it)) },
                placeholder = "Document title",
                modifier = Modifier.fillMaxWidth().testTag(DemoTestTags.DocumentsNewTitle),
            )
            if (isWide) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(ShellSpacing.lg),
                ) {
                    BasicTextField(
                        value = state.newDocumentMarkdown,
                        onValueChange = { onIntent(DocumentsIntent.NewDocumentMarkdownChanged(it)) },
                        textStyle = ShellTheme.typography.code.copy(color = colors.textPrimary),
                        cursorBrush = SolidColor(colors.info),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .testTag(DemoTestTags.DocumentsNewMarkdown)
                            .clip(RoundedCornerShape(ShellRadius.md))
                            .background(colors.surfaceSubtle)
                            .border(1.dp, colors.info.copy(alpha = 0.35f), RoundedCornerShape(ShellRadius.md))
                            .padding(ShellSpacing.lg)
                            .verticalScroll(rememberScrollState()),
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
                            color = colors.textMuted,
                            modifier = Modifier.padding(bottom = ShellSpacing.sm),
                        )
                        LiveMarkdownPreview(markdown = state.newDocumentMarkdown)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(ShellSpacing.lg),
                ) {
                    BasicTextField(
                        value = state.newDocumentMarkdown,
                        onValueChange = { onIntent(DocumentsIntent.NewDocumentMarkdownChanged(it)) },
                        textStyle = ShellTheme.typography.code.copy(color = colors.textPrimary),
                        cursorBrush = SolidColor(colors.info),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 240.dp)
                            .testTag(DemoTestTags.DocumentsNewMarkdown)
                            .clip(RoundedCornerShape(ShellRadius.md))
                            .background(colors.surfaceSubtle)
                            .border(1.dp, colors.info.copy(alpha = 0.35f), RoundedCornerShape(ShellRadius.md))
                            .padding(ShellSpacing.lg)
                            .verticalScroll(rememberScrollState()),
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Text(
                            text = "Preview",
                            style = ShellTheme.typography.label,
                            color = colors.textMuted,
                            modifier = Modifier.padding(bottom = ShellSpacing.sm),
                        )
                        LiveMarkdownPreview(markdown = state.newDocumentMarkdown)
                    }
                }
            }
        }
    }
}
