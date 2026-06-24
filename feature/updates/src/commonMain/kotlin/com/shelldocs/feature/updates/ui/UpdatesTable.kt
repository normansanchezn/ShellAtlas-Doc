package com.shelldocs.feature.updates.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.*
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.updates.PendingUpdate
import com.shelldocs.core.domain.entity.updates.RiskLevel
import com.shelldocs.feature.updates.presentation.UpdatesState
import kotlin.time.ExperimentalTime

/** Triage table: document, area, risk, version drift, owner, last review, actions. */
@Composable
fun UpdatesTable(
    state: UpdatesState,
    isWide: Boolean,
    onSetRisk: (documentId: String, risk: RiskLevel?) -> Unit,
    onOpenUpdate: (documentId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    val showActions = state.isAdmin || state.canUpdateDocuments
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
                HeaderCell("RISK", Modifier.width(86.dp))
                if (isWide) HeaderCell("APP VERSION", Modifier.width(96.dp))
                if (isWide) HeaderCell("DOC VERSION", Modifier.width(96.dp))
                HeaderCell("OWNER", Modifier.width(60.dp))
                if (isWide) HeaderCell("LAST REVIEW", Modifier.width(96.dp))
                if (showActions) HeaderCell("ACTIONS", Modifier.width(260.dp))
            }
            state.filteredUpdates.forEach { update ->
                UpdateRow(
                    update = update,
                    isWide = isWide,
                    isAdmin = state.isAdmin,
                    canUpdateDocuments = state.canUpdateDocuments,
                    onSetRisk = onSetRisk,
                    onOpenUpdate = onOpenUpdate,
                )
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier) {
    Text(
        text = text,
        style = ShellTheme.typography.sectionLabel,
        color = ShellTheme.colors.textMuted,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@OptIn(ExperimentalTime::class)
@Composable
private fun UpdateRow(
    update: PendingUpdate,
    isWide: Boolean,
    isAdmin: Boolean,
    canUpdateDocuments: Boolean,
    onSetRisk: (documentId: String, risk: RiskLevel?) -> Unit,
    onOpenUpdate: (documentId: String) -> Unit,
) {
    val colors = ShellTheme.colors
    val versionMismatch = update.documentVersion.isNotBlank() && update.documentVersion != update.applicationVersion
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ShellSpacing.lg, vertical = ShellSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(2.2f)) {
            Text(update.documentTitle, style = ShellTheme.typography.bodyStrong, color = colors.textPrimary)
            Text(update.module, style = ShellTheme.typography.caption, color = colors.textMuted)
        }
        if (isWide) {
            Text(
                text = update.area?.displayName ?: "—",
                style = ShellTheme.typography.label,
                color = colors.textSecondary,
                modifier = Modifier.weight(1.1f),
            )
        }
        Box(modifier = Modifier.width(86.dp)) { ShellRiskBadge(risk = update.risk) }
        if (isWide) {
            Text(
                text = update.applicationVersion.ifBlank { "—" },
                style = ShellTheme.typography.label,
                color = colors.textSecondary,
                modifier = Modifier.width(96.dp),
            )
        }
        if (isWide) {
            Text(
                text = update.documentVersion.ifBlank { "—" },
                style = ShellTheme.typography.label,
                color = if (versionMismatch) colors.danger else colors.textSecondary,
                modifier = Modifier.width(96.dp),
            )
        }
        Box(modifier = Modifier.width(60.dp)) {
            ShellAvatar(
                initials = update.ownerInitials,
                size = 20.dp,
                color = colors.surfaceSubtle,
                contentColor = colors.textSecondary,
            )
        }
        if (isWide) {
            Text(
                text = update.lastReview.toString().substringBefore('T'),
                style = ShellTheme.typography.caption,
                color = colors.textMuted,
                modifier = Modifier.width(96.dp),
            )
        }
        if (isAdmin || canUpdateDocuments) {
            Row(
                modifier = Modifier.width(260.dp),
                horizontalArrangement = Arrangement.spacedBy(ShellSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isAdmin) {
                    ShellDropdown(
                        selected = update.risk,
                        options = RiskLevel.DOCUMENTATION_HEALTH_LEVELS,
                        label = { it.displayName },
                        onSelect = { risk -> onSetRisk(update.documentId, risk) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (canUpdateDocuments) {
                    ShellGhostButton(text = "Update", onClick = { onOpenUpdate(update.documentId) })
                }
            }
        }
    }
}
