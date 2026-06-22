package com.shelldocs.feature.dashboard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellAvatar
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.dashboard.DashboardMetrics

/** Top Owners: who holds the most documents right now. */
@Composable
fun TopOwnersCard(metrics: DashboardMetrics, modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    ShellCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(ShellSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(ShellSpacing.md),
        ) {
            Text("Top Owners", style = ShellTheme.typography.sectionTitle, color = colors.textPrimary)
            if (metrics.topOwners.isEmpty()) {
                Text(
                    text = "No documents have an owner assigned yet.",
                    style = ShellTheme.typography.caption,
                    color = colors.textMuted,
                )
            }
            metrics.topOwners.forEach { owner ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
                ) {
                    ShellAvatar(initials = initials(owner.owner), size = 28.dp)
                    Text(
                        text = owner.owner,
                        style = ShellTheme.typography.label,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${owner.documentCount} docs",
                        style = ShellTheme.typography.caption,
                        color = colors.textMuted,
                    )
                }
            }
        }
    }
}

private fun initials(name: String): String =
    name.split(' ').filter { it.isNotBlank() }.take(2).map { it.first().uppercaseChar() }.joinToString("")
