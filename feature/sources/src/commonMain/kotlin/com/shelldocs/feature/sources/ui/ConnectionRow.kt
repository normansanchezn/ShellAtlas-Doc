package com.shelldocs.feature.sources.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellBadge
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.icons.*
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.connection.ConnectionState
import com.shelldocs.core.domain.entity.connection.ConnectionStatus

/** One system's real-time health: icon, name, status badge, optional detail. */
@Composable
fun ConnectionRow(connection: ConnectionStatus, modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    ShellCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(ShellSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(ShellRadius.md))
                    .background(colors.surfaceSubtle),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = when (connection.id) {
                        "ollama" -> IconSparkles
                        "confluence" -> IconLayers
                        "azure-devops" -> IconGitBranch
                        "jira" -> IconBookOpen
                        else -> IconDatabase
                    },
                    contentDescription = null,
                    tint = colors.info,
                    modifier = Modifier.size(16.dp),
                )
            }
            Column(modifier = Modifier.weight(1f).padding(start = ShellSpacing.md)) {
                Text(text = connection.name, style = ShellTheme.typography.bodyStrong, color = colors.textPrimary)
                connection.detail?.let {
                    Text(text = it, style = ShellTheme.typography.caption, color = colors.textMuted)
                }
            }
            Box(modifier = Modifier.testTag("connection-status-${connection.id}")) {
                when (connection.state) {
                    ConnectionState.CONNECTED -> ShellBadge(
                        text = "✓ Connected",
                        contentColor = colors.success,
                        containerColor = colors.successSoft,
                    )

                    ConnectionState.ERROR -> ShellBadge(
                        text = "✕ Error",
                        contentColor = colors.danger,
                        containerColor = colors.dangerSoft,
                    )

                    ConnectionState.DISCONNECTED -> ShellBadge(
                        text = "Disconnected",
                        contentColor = colors.textMuted,
                        containerColor = colors.surfaceSubtle,
                    )

                    ConnectionState.DISABLED -> ShellBadge(
                        text = "Disabled",
                        contentColor = colors.textMuted,
                        containerColor = colors.surfaceSubtle,
                    )
                }
            }
        }
    }
}
