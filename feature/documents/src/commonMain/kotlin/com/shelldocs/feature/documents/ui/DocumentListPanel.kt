package com.shelldocs.feature.documents.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellAvatar
import com.shelldocs.core.designsystem.atoms.ShellStatusBadge
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.feature.documents.presentation.DocumentsIntent
import com.shelldocs.feature.documents.presentation.DocumentsState

/** Middle pane: "N docs" header + document cards with status badges. */
@Composable
fun DocumentListPanel(
    state: DocumentsState,
    onIntent: (DocumentsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Column(modifier = modifier.background(colors.background)) {
        Text(
            text = "${state.filteredDocuments.size} docs",
            style = ShellTheme.typography.sectionTitle,
            color = colors.textPrimary,
            modifier = Modifier.padding(ShellSpacing.md),
        )
        LazyColumn {
            items(state.filteredDocuments.size, key = { state.filteredDocuments[it].id }) { index ->
                DocumentListRow(
                    document = state.filteredDocuments[index],
                    isSelected = state.filteredDocuments[index].id == state.selectedDocument?.id,
                    onClick = { onIntent(DocumentsIntent.SelectDocument(state.filteredDocuments[index].id)) },
                )
            }
        }
    }
}

@Composable
private fun DocumentListRow(
    document: Document,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = ShellTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) colors.surfaceSelected else colors.background)
            .clickable(onClick = onClick)
            .padding(horizontal = ShellSpacing.md, vertical = ShellSpacing.sm + 2.dp),
        verticalArrangement = Arrangement.spacedBy(ShellSpacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = document.title,
                style = ShellTheme.typography.bodyStrong,
                color = if (isSelected) colors.brand else colors.textPrimary,
                maxLines = 1,
                modifier = Modifier.weight(1f, fill = false),
            )
            ShellStatusBadge(status = document.status)
        }
        Text(
            text = document.summary,
            style = ShellTheme.typography.caption,
            color = colors.textMuted,
            maxLines = 2,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ShellSpacing.xs),
        ) {
            ShellAvatar(
                initials = initialsOf(document.attributes.owner),
                size = 14.dp,
                color = colors.surfaceSubtle,
                contentColor = colors.textSecondary,
            )
            Text(
                text = document.attributes.owner,
                style = ShellTheme.typography.caption,
                color = colors.textMuted,
            )
        }
    }
}

private fun initialsOf(name: String): String =
    name.split(' ').filter { it.isNotBlank() }.take(2)
        .map { it.first().uppercaseChar() }.joinToString("").ifEmpty { "?" }
