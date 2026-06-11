package com.shelldocs.feature.updates.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import com.shelldocs.core.designsystem.atoms.ShellRiskBadge
import com.shelldocs.core.designsystem.molecules.ShellProgressBar
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.updates.PendingUpdate
import com.shelldocs.core.domain.entity.updates.RiskLevel
import com.shelldocs.feature.updates.presentation.UpdatesState
import kotlin.time.ExperimentalTime

/** Triage table: document, team, risk, age, impact, owner, last review. */
@Composable
fun UpdatesTable(
    state: UpdatesState,
    isWide: Boolean,
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
                HeaderCell("DOCUMENT", Modifier.weight(2.4f))
                if (isWide) HeaderCell("TEAM", Modifier.weight(1.2f))
                HeaderCell("RISK", Modifier.width(86.dp))
                HeaderCell("AGE", Modifier.width(56.dp))
                if (isWide) HeaderCell("IMPACT", Modifier.width(110.dp))
                HeaderCell("OWNER", Modifier.width(60.dp))
                if (isWide) HeaderCell("LAST REVIEW", Modifier.width(96.dp))
            }
            state.filteredUpdates.forEach { update ->
                UpdateRow(update = update, isWide = isWide)
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
private fun UpdateRow(update: PendingUpdate, isWide: Boolean) {
    val colors = ShellTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ShellSpacing.lg, vertical = ShellSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(2.4f)) {
            Text(update.documentTitle, style = ShellTheme.typography.bodyStrong, color = colors.textPrimary)
            Text(update.module, style = ShellTheme.typography.caption, color = colors.textMuted)
        }
        if (isWide) {
            Text(
                text = update.team,
                style = ShellTheme.typography.label,
                color = colors.textSecondary,
                modifier = Modifier.weight(1.2f),
            )
        }
        Box(modifier = Modifier.width(86.dp)) { ShellRiskBadge(risk = update.risk) }
        Text(
            text = "${update.ageDays}d",
            style = ShellTheme.typography.label,
            color = riskTint(update.risk),
            modifier = Modifier.width(56.dp),
        )
        if (isWide) {
            Row(
                modifier = Modifier.width(110.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ShellSpacing.xs),
            ) {
                ShellProgressBar(
                    progress = update.impactScore / 100f,
                    color = riskTint(update.risk),
                    modifier = Modifier.width(46.dp),
                )
                Text("${update.impactScore}", style = ShellTheme.typography.caption, color = colors.textSecondary)
            }
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
    }
}

@Composable
private fun riskTint(risk: RiskLevel): androidx.compose.ui.graphics.Color {
    val colors = ShellTheme.colors
    return when (risk) {
        RiskLevel.CRITICAL -> colors.danger
        RiskLevel.HIGH -> colors.warning
        RiskLevel.MEDIUM -> colors.brand
        RiskLevel.LOW -> colors.info
    }
}
