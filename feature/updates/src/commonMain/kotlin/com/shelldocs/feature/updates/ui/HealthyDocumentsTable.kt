package com.shelldocs.feature.updates.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellAvatar
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.updates.PendingUpdate
import kotlin.time.ExperimentalTime

/** Informational only: Document / Area / Version / Owner / Last Review / Open. No maintenance actions. */
@Composable
fun HealthyDocumentsTable(
    documents: List<PendingUpdate>,
    isWide: Boolean,
    onOpenDocument: (documentId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    ShellCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceSubtle)
                    .padding(horizontal = ShellSpacing.lg, vertical = ShellSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HeaderCell("DOCUMENT", Modifier.weight(2.2f))
                if (isWide) HeaderCell("AREA", Modifier.weight(1.1f))
                if (isWide) HeaderCell("VERSION", Modifier.width(96.dp))
                HeaderCell("OWNER", Modifier.width(60.dp))
                if (isWide) HeaderCell("LAST REVIEW", Modifier.width(96.dp))
                HeaderCell("ACTION", Modifier.width(110.dp))
            }
            documents.forEach { document -> HealthyDocumentRow(document, isWide, onOpenDocument) }
        }
    }
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier) {
    Text(
        text = text,
        style = ShellTheme.typography.sectionLabel,
        color = ShellTheme.colors.textMuted,
        modifier = modifier
    )
}

@OptIn(ExperimentalTime::class)
@Composable
private fun HealthyDocumentRow(
    document: PendingUpdate,
    isWide: Boolean,
    onOpenDocument: (documentId: String) -> Unit,
) {
    val colors = ShellTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ShellSpacing.lg, vertical = ShellSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(2.2f)) {
            Text(document.documentTitle, style = ShellTheme.typography.bodyStrong, color = colors.textPrimary)
            Text(document.module, style = ShellTheme.typography.caption, color = colors.textMuted)
        }
        if (isWide) {
            Text(
                text = document.area?.displayName ?: "—",
                style = ShellTheme.typography.label,
                color = colors.textSecondary,
                modifier = Modifier.weight(1.1f),
            )
        }
        if (isWide) {
            Text(
                text = document.documentVersion.ifBlank { "—" },
                style = ShellTheme.typography.label,
                color = colors.textSecondary,
                modifier = Modifier.width(96.dp),
            )
        }
        Box(modifier = Modifier.width(60.dp)) {
            ShellAvatar(
                initials = document.ownerInitials,
                size = 20.dp,
                color = colors.surfaceSubtle,
                contentColor = colors.textSecondary,
            )
        }
        if (isWide) {
            Text(
                text = document.lastReview.toString().substringBefore('T'),
                style = ShellTheme.typography.caption,
                color = colors.textMuted,
                modifier = Modifier.width(96.dp),
            )
        }
        Box(modifier = Modifier.width(110.dp)) {
            ShellGhostButton(text = "Open Document", onClick = { onOpenDocument(document.documentId) })
        }
    }
}
