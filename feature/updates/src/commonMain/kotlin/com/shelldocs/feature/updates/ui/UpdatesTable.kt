package com.shelldocs.feature.updates.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellAvatar
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.atoms.ShellRiskBadge
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.updates.PendingUpdate
import com.shelldocs.core.domain.entity.updates.RiskLevel
import com.shelldocs.feature.updates.presentation.UpdatesState
import kotlin.time.ExperimentalTime

/** Triage table: document, development area, risk, version drift, owner, last review, actions. */
@Composable
fun UpdatesTable(
    state: UpdatesState,
    isWide: Boolean,
    onSetRisk: (documentId: String, risk: RiskLevel?) -> Unit,
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
            ) {
                HeaderCell("DOCUMENT", Modifier.weight(2.2f))
                if (isWide) HeaderCell("AREA", Modifier.weight(1.1f))
                HeaderCell("RISK", Modifier.width(86.dp))
                if (isWide) HeaderCell("APP VERSION", Modifier.width(96.dp))
                if (isWide) HeaderCell("DOC VERSION", Modifier.width(96.dp))
                HeaderCell("OWNER", Modifier.width(60.dp))
                if (isWide) HeaderCell("LAST REVIEW", Modifier.width(96.dp))
                if (state.isAdmin) HeaderCell("ACTIONS", Modifier.width(130.dp))
            }
            state.filteredUpdates.forEach { update ->
                UpdateRow(update = update, isWide = isWide, isAdmin = state.isAdmin, onSetRisk = onSetRisk)
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
        modifier = modifier,
    )
}

@OptIn(ExperimentalTime::class)
@Composable
private fun UpdateRow(
    update: PendingUpdate,
    isWide: Boolean,
    isAdmin: Boolean,
    onSetRisk: (documentId: String, risk: RiskLevel?) -> Unit,
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
                text = update.developmentArea?.displayName ?: "—",
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
        if (isAdmin) {
            Box(modifier = Modifier.width(130.dp)) {
                ShellGhostButton(
                    text = if (update.manualRiskOverride == RiskLevel.MEDIUM) "Clear override" else "Mark Medium",
                    onClick = {
                        val next = if (update.manualRiskOverride == RiskLevel.MEDIUM) null else RiskLevel.MEDIUM
                        onSetRisk(update.documentId, next)
                    },
                )
            }
        }
    }
}
