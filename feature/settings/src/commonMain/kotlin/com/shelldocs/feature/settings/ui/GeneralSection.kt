package com.shelldocs.feature.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.shelldocs.core.common.testing.DemoTestTags
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.icons.IconLogOut
import com.shelldocs.core.designsystem.molecules.ShellToggle
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing

/** General: appearance and session controls. */
@Composable
fun GeneralSection(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Column(modifier = modifier.widthIn(max = 520.dp)) {
        Text("General", style = ShellTheme.typography.sectionTitle, color = colors.textPrimary)
        ShellCard(modifier = Modifier.fillMaxWidth().padding(top = ShellSpacing.md)) {
            Column(modifier = Modifier.fillMaxWidth().padding(ShellSpacing.lg)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dark mode", style = ShellTheme.typography.bodyStrong, color = colors.textPrimary)
                        Text(
                            "Switch between the light and dark Figma palettes",
                            style = ShellTheme.typography.caption,
                            color = colors.textMuted,
                        )
                    }
                    ShellToggle(checked = isDarkTheme, onCheckedChange = { onToggleTheme() })
                }
            }
        }
        ShellGhostButton(
            text = "Sign out",
            icon = IconLogOut,
            onClick = onSignOut,
            modifier = Modifier.padding(top = ShellSpacing.lg).testTag(DemoTestTags.SettingsSignOut),
        )
    }
}
