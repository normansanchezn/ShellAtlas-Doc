package com.shelldocs.feature.documents.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.atoms.ShellStatusBadge
import com.shelldocs.core.designsystem.icons.IconBookmark
import com.shelldocs.core.designsystem.icons.IconEdit
import com.shelldocs.core.designsystem.icons.IconHistory
import com.shelldocs.core.designsystem.icons.IconShare
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.feature.documents.presentation.DocumentsIntent
import com.shelldocs.feature.documents.presentation.DocumentsState
import kotlin.time.ExperimentalTime

/** Reader: breadcrumb bar, rendered Markdown body and the attributes rail. */
@OptIn(ExperimentalTime::class)
@Composable
fun DocumentReaderPanel(
    state: DocumentsState,
    document: Document,
    isWide: Boolean,
    onIntent: (DocumentsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ShellSpacing.lg, vertical = ShellSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
        ) {
            Text(
                text = "Docs  ›  ${document.attributes.platform}  ›  ${document.title}",
                style = ShellTheme.typography.caption,
                color = colors.textMuted,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            ShellGhostButton(
                text = "History",
                icon = IconHistory,
                onClick = { onIntent(DocumentsIntent.ShowHistory) },
            )
            if (state.canEdit) {
                ShellPrimaryButton(
                    text = "Edit",
                    icon = IconEdit,
                    onClick = { onIntent(DocumentsIntent.StartEditing) },
                )
            }
        }
        Row(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ShellSpacing.xxxl, vertical = ShellSpacing.xl),
            ) {
                ShellStatusBadge(status = document.status)
                Text(
                    text = document.title,
                    style = ShellTheme.typography.displayTitle,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(top = ShellSpacing.md),
                )
                Text(
                    text = document.summary,
                    style = ShellTheme.typography.body,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = ShellSpacing.xs),
                )
                Text(
                    text = "${document.attributes.owner}  ·  Updated ${document.updatedAt.toString().substringBefore('T')}",
                    style = ShellTheme.typography.caption,
                    color = colors.textMuted,
                    modifier = Modifier.padding(top = ShellSpacing.md, bottom = ShellSpacing.lg),
                )
                MarkdownBlocksView(
                    blocks = document.content.blocks,
                    modifier = Modifier.widthIn(max = 680.dp),
                )
                Row(
                    modifier = Modifier.padding(top = ShellSpacing.xl),
                    horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
                ) {
                    ShellGhostButton(text = "Share", icon = IconShare, onClick = {})
                    ShellGhostButton(text = "Bookmark", icon = IconBookmark, onClick = {})
                }
            }
            if (isWide) {
                if (state.isHistoryVisible) {
                    VersionHistoryPanel(
                        state = state,
                        onIntent = onIntent,
                        modifier = Modifier.width(250.dp).fillMaxHeight(),
                    )
                } else {
                    AttributesPanel(
                        document = document,
                        modifier = Modifier.width(220.dp).fillMaxHeight(),
                    )
                }
            }
        }
        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage.orEmpty(),
                style = ShellTheme.typography.caption,
                color = colors.danger,
                modifier = Modifier.padding(ShellSpacing.md),
            )
        }
    }
}
