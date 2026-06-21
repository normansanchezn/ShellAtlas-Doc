package com.shelldocs.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.shelldocs.core.common.testing.DemoTestTags
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.i18n.AppStrings
import com.shelldocs.core.designsystem.icons.IconLogOut
import com.shelldocs.core.designsystem.molecules.ShellToggle
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.auth.AppLanguage

/** General: appearance, language and session controls. */
@Composable
fun GeneralSection(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onSignOut: () -> Unit,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    strings: AppStrings,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Column(modifier = modifier.widthIn(max = 520.dp)) {
        Text(strings.settingsGeneralTitle, style = ShellTheme.typography.sectionTitle, color = colors.textPrimary)
        ShellCard(modifier = Modifier.fillMaxWidth().padding(top = ShellSpacing.md)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(ShellSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(ShellSpacing.lg),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            strings.settingsDarkMode,
                            style = ShellTheme.typography.bodyStrong,
                            color = colors.textPrimary
                        )
                        Text(
                            strings.settingsDarkModeDescription,
                            style = ShellTheme.typography.caption,
                            color = colors.textMuted,
                        )
                    }
                    ShellToggle(checked = isDarkTheme, onCheckedChange = { onToggleTheme() })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            strings.settingsLanguage,
                            style = ShellTheme.typography.bodyStrong,
                            color = colors.textPrimary
                        )
                        Text(
                            strings.settingsLanguageDescription,
                            style = ShellTheme.typography.caption,
                            color = colors.textMuted,
                        )
                    }
                    LanguagePicker(
                        selected = language,
                        strings = strings,
                        onSelect = onLanguageChange,
                    )
                }
            }
        }
        ShellGhostButton(
            text = strings.settingsSignOut,
            icon = IconLogOut,
            onClick = onSignOut,
            modifier = Modifier.padding(top = ShellSpacing.lg).testTag(DemoTestTags.SettingsSignOut),
        )
    }
}

@Composable
private fun LanguagePicker(
    selected: AppLanguage,
    strings: AppStrings,
    onSelect: (AppLanguage) -> Unit,
) {
    val colors = ShellTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(ShellSpacing.xs)) {
        AppLanguage.entries.forEach { language ->
            val isActive = language == selected
            val label = when (language) {
                AppLanguage.ENGLISH -> strings.languageEnglish
                AppLanguage.SPANISH -> strings.languageSpanish
                AppLanguage.FRENCH -> strings.languageFrench
            }
            Text(
                text = label,
                style = ShellTheme.typography.caption,
                color = if (isActive) colors.onBrand else colors.textSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(ShellRadius.full))
                    .background(if (isActive) colors.brand else colors.surfaceSubtle)
                    .clickable { onSelect(language) }
                    .padding(horizontal = ShellSpacing.sm, vertical = ShellSpacing.xs),
            )
        }
    }
}
