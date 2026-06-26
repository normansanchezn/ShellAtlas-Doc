package com.shelldocs.feature.dashboard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.molecules.ShellHealthRing
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.designsystem.tokens.StringLocalRes.KT_TXT
import com.shelldocs.core.domain.entity.dashboard.DashboardMetrics

/** Knowledge Transfer completion ring for the current user. */
@Composable
fun KnowledgeTransferCard(metrics: DashboardMetrics, modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    ShellCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(ShellSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ShellHealthRing(score = metrics.knowledgeTransferPercent, modifier = Modifier.size(120.dp))
            Text(
                text = KT_TXT,
                style = ShellTheme.typography.sectionTitle,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = ShellSpacing.md),
            )
            Text(
                text = "${metrics.knowledgeTransferCompleted}/${metrics.knowledgeTransferTotal} checkpoints completed",
                style = ShellTheme.typography.caption,
                color = colors.textMuted,
            )
        }
    }
}
