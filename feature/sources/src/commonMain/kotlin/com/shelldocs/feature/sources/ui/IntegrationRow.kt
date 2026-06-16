package com.shelldocs.feature.sources.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.shelldocs.core.common.testing.DemoTestTags
import com.shelldocs.core.designsystem.atoms.ShellBadge
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.icons.IconBookOpen
import com.shelldocs.core.designsystem.icons.IconGitBranch
import com.shelldocs.core.designsystem.icons.IconLayers
import com.shelldocs.core.designsystem.icons.IconRefresh
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.source.KnowledgeSource
import com.shelldocs.core.domain.entity.source.SourceKind
import com.shelldocs.core.domain.entity.source.SourceStatus

/** Integration card: icon, host, status, doc count, sync/reconnect actions. */
@Composable
fun IntegrationRow(
    source: KnowledgeSource,
    isSyncing: Boolean,
    onSync: () -> Unit,
    onReconnect: () -> Unit,
    actionsEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    ShellCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(ShellSpacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(ShellRadius.md))
                        .background(colors.surfaceSubtle),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = when (source.kind) {
                            SourceKind.CONFLUENCE -> IconLayers
                            SourceKind.AZURE_DEVOPS -> IconGitBranch
                            SourceKind.JIRA -> IconBookOpen
                        },
                        contentDescription = null,
                        tint = colors.info,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f).padding(start = ShellSpacing.md)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
                    ) {
                        Text(
                            text = source.kind.displayName,
                            style = ShellTheme.typography.bodyStrong,
                            color = colors.textPrimary,
                        )
                        when (source.status) {
                            SourceStatus.CONNECTED -> ShellBadge(
                                text = "✓ Connected",
                                contentColor = colors.success,
                                containerColor = colors.successSoft,
                            )
                            SourceStatus.ERROR -> ShellBadge(
                                text = "✕ Error",
                                contentColor = colors.danger,
                                containerColor = colors.dangerSoft,
                            )
                            SourceStatus.DISCONNECTED -> ShellBadge(
                                text = "Disconnected",
                                contentColor = colors.textMuted,
                                containerColor = colors.surfaceSubtle,
                            )
                        }
                    }
                    Text(text = source.host, style = ShellTheme.typography.caption, color = colors.textMuted)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${source.importedDocs}",
                        style = ShellTheme.typography.sectionTitle,
                        color = colors.textPrimary,
                    )
                    Text(text = "docs", style = ShellTheme.typography.caption, color = colors.textMuted)
                }
                Row(
                    modifier = Modifier.padding(start = ShellSpacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
                ) {
                    ShellGhostButton(
                        text = if (isSyncing) "Syncing..." else "Sync",
                        icon = IconRefresh,
                        onClick = onSync,
                        enabled = actionsEnabled && !isSyncing,
                        modifier = Modifier.testTag(DemoTestTags.sourceSync(source.kind.displayName)),
                    )
                    if (source.status == SourceStatus.ERROR) {
                        ShellPrimaryButton(
                            text = "Reconnect",
                            onClick = onReconnect,
                            enabled = actionsEnabled && !isSyncing,
                            modifier = Modifier.testTag(DemoTestTags.sourceReconnect(source.kind.displayName)),
                        )
                    }
                }
            }
        }
    }
}
