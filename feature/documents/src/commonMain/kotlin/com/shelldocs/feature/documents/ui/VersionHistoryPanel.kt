package com.shelldocs.feature.documents.ui

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
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.atoms.ShellIconButton
import com.shelldocs.core.designsystem.atoms.ShellSectionLabel
import com.shelldocs.core.designsystem.icons.IconX
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.feature.documents.presentation.DocumentsIntent
import com.shelldocs.feature.documents.presentation.DocumentsState
import kotlin.time.ExperimentalTime

/** Version history rail with per-version restore. */
@OptIn(ExperimentalTime::class)
@Composable
fun VersionHistoryPanel(
    state: DocumentsState,
    onIntent: (DocumentsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Column(modifier = modifier.padding(ShellSpacing.lg)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShellSectionLabel(text = "History", modifier = Modifier.weight(1f))
            ShellIconButton(
                icon = IconX,
                contentDescription = "Close history",
                onClick = { onIntent(DocumentsIntent.HideHistory) },
            )
        }
        LazyColumn(
            modifier = Modifier.padding(top = ShellSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
        ) {
            items(state.versions.size, key = { state.versions[it].id }) { index ->
                val version = state.versions[index]
                ShellCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(ShellSpacing.md)) {
                        Text(
                            text = "v${version.versionNumber} · ${version.changeSummary}",
                            style = ShellTheme.typography.label,
                            color = colors.textPrimary,
                        )
                        Text(
                            text = version.createdAt.toString().substringBefore('T'),
                            style = ShellTheme.typography.caption,
                            color = colors.textMuted,
                        )
                        if (state.canEdit && index != 0) {
                            ShellGhostButton(
                                text = "Restore",
                                onClick = { onIntent(DocumentsIntent.RestoreVersion(version.id)) },
                                modifier = Modifier.padding(top = ShellSpacing.sm),
                            )
                        }
                    }
                }
            }
        }
    }
}
