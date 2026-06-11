package com.shelldocs.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.molecules.ShellToggle
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.feature.settings.presentation.SettingsIntent
import com.shelldocs.feature.settings.presentation.SettingsState

/** Notifications: per-event toggles. */
@Composable
fun NotificationsSection(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Column(modifier = modifier.widthIn(max = 520.dp)) {
        Text("Notifications", style = ShellTheme.typography.sectionTitle, color = colors.textPrimary)
        ShellCard(modifier = Modifier.fillMaxWidth().padding(top = ShellSpacing.md)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(ShellSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(ShellSpacing.md),
            ) {
                NotificationToggleRow(
                    title = "Outdated documents",
                    subtitle = "Alert when documents are flagged outdated",
                    checked = state.notifyOutdatedDocs,
                    onCheckedChange = { onIntent(SettingsIntent.SetNotifyOutdatedDocs(it)) },
                )
                NotificationToggleRow(
                    title = "Sync failures",
                    subtitle = "Alert when an integration sync fails",
                    checked = state.notifySyncFailures,
                    onCheckedChange = { onIntent(SettingsIntent.SetNotifySyncFailures(it)) },
                )
                NotificationToggleRow(
                    title = "Weekly digest",
                    subtitle = "Summary of knowledge health every Monday",
                    checked = state.notifyWeeklyDigest,
                    onCheckedChange = { onIntent(SettingsIntent.SetNotifyWeeklyDigest(it)) },
                )
            }
        }
    }
}

@Composable
private fun NotificationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = ShellTheme.colors
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = ShellTheme.typography.bodyStrong, color = colors.textPrimary)
            Text(subtitle, style = ShellTheme.typography.caption, color = colors.textMuted)
        }
        ShellToggle(checked = checked, onCheckedChange = onCheckedChange)
    }
}
