package com.shelldocs.feature.settings.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.molecules.ShellErrorDialog
import com.shelldocs.core.designsystem.molecules.ShellLoadingOverlay
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellMotion
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.feature.settings.presentation.SettingsEffect
import com.shelldocs.feature.settings.presentation.SettingsIntent
import com.shelldocs.feature.settings.presentation.SettingsSection
import com.shelldocs.feature.settings.presentation.SettingsViewModel

/** Settings: section rail + the selected section's panel. */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    isWide: Boolean,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val colors = ShellTheme.colors

    LaunchedEffect(viewModel) {
        viewModel.onIntent(SettingsIntent.Initialize)
        viewModel.effects.collect { effect ->
            when (effect) {
                SettingsEffect.SignedOut -> onSignedOut()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(ShellSpacing.lg)) {
                Text("Settings", style = ShellTheme.typography.pageTitle, color = colors.textPrimary)
                Text(
                    "Platform configuration and preferences",
                    style = ShellTheme.typography.caption,
                    color = colors.textMuted,
                )
            }
            Row(modifier = Modifier.weight(1f)) {
                if (isWide) {
                    SectionRail(
                        selected = state.selectedSection,
                        onSelect = { viewModel.onIntent(SettingsIntent.SelectSection(it)) },
                        modifier = Modifier.width(180.dp).fillMaxHeight().padding(horizontal = ShellSpacing.lg),
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = ShellSpacing.xl, vertical = ShellSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(ShellSpacing.lg),
                ) {
                    if (!isWide) {
                        SectionChipsRow(
                            selected = state.selectedSection,
                            onSelect = { viewModel.onIntent(SettingsIntent.SelectSection(it)) },
                        )
                    }
                    when (state.selectedSection) {
                        SettingsSection.GENERAL -> GeneralSection(
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = onToggleTheme,
                            onSignOut = { viewModel.onIntent(SettingsIntent.SignOut) },
                        )
                        SettingsSection.AI_ASSISTANT -> AiAssistantSection()
                        SettingsSection.TEAM_AND_ACCESS -> TeamAccessSection(
                            state = state,
                            onIntent = viewModel::onIntent,
                        )
                        SettingsSection.NOTIFICATIONS -> NotificationsSection(
                            state = state,
                            onIntent = viewModel::onIntent,
                        )
                        SettingsSection.INTEGRATIONS -> IntegrationsSection()
                    }
                }
            }
        }

        state.loadingMessage?.let { message ->
            ShellLoadingOverlay(message = message)
        }
    }

    state.errorDialog?.let { dialog ->
        ShellErrorDialog(
            state = dialog,
            onDismiss = { viewModel.onIntent(SettingsIntent.DismissError) },
        )
    }
}

@Composable
private fun SectionRail(
    selected: SettingsSection,
    onSelect: (SettingsSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        SettingsSection.entries.forEach { section ->
            val isActive = section == selected
            val background by animateColorAsState(
                targetValue = if (isActive) colors.surfaceSelected else colors.background,
                animationSpec = tween(ShellMotion.durationMedium),
                label = "settingsRailBackground",
            )
            val contentColor by animateColorAsState(
                targetValue = if (isActive) colors.brand else colors.textSecondary,
                animationSpec = tween(ShellMotion.durationMedium),
                label = "settingsRailContent",
            )
            Text(
                text = section.displayName,
                style = ShellTheme.typography.label,
                color = contentColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 32.dp)
                    .clip(RoundedCornerShape(ShellRadius.sm))
                    .background(background)
                    .clickable { onSelect(section) }
                    .padding(horizontal = ShellSpacing.md, vertical = ShellSpacing.sm),
            )
        }
    }
}

@Composable
private fun SectionChipsRow(
    selected: SettingsSection,
    onSelect: (SettingsSection) -> Unit,
) {
    val colors = ShellTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(ShellSpacing.xs)) {
        SettingsSection.entries.forEach { section ->
            val isActive = section == selected
            val background by animateColorAsState(
                targetValue = if (isActive) colors.brand else colors.surfaceSubtle,
                animationSpec = tween(ShellMotion.durationMedium),
                label = "settingsChipBackground",
            )
            val contentColor by animateColorAsState(
                targetValue = if (isActive) colors.onBrand else colors.textSecondary,
                animationSpec = tween(ShellMotion.durationMedium),
                label = "settingsChipContent",
            )
            Text(
                text = section.displayName,
                style = ShellTheme.typography.caption,
                color = contentColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(ShellRadius.full))
                    .background(background)
                    .clickable { onSelect(section) }
                    .padding(horizontal = ShellSpacing.sm, vertical = ShellSpacing.xs),
            )
        }
    }
}
