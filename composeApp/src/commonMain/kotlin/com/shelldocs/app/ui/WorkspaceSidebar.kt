package com.shelldocs.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.shelldocs.app.navigation.AppRoute
import com.shelldocs.app.strings.StringRes.APP_BRAND_NAME
import com.shelldocs.app.strings.StringRes.CONFLUENCE_TXT
import com.shelldocs.app.strings.StringRes.DARK_MODE_TXT
import com.shelldocs.app.strings.StringRes.LIGHT_MODE_TXT
import com.shelldocs.app.strings.StringRes.SEARCH_SHORTCUT
import com.shelldocs.app.strings.StringRes.SEARCH_TXT
import com.shelldocs.core.common.testing.DemoTestTags
import com.shelldocs.core.designsystem.atoms.ShellAvatar
import com.shelldocs.core.designsystem.atoms.ShellBadge
import com.shelldocs.core.designsystem.atoms.ShellBrandBadge
import com.shelldocs.core.designsystem.atoms.ShellSectionLabel
import com.shelldocs.core.designsystem.icons.*
import com.shelldocs.core.designsystem.molecules.ShellKbdHint
import com.shelldocs.core.designsystem.molecules.ShellSearchField
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellMotion
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.auth.UserProfile

/**
 * 240dp sidebar from the Figma AppShell: brand header, search, KNOWLEDGE /
 * ANALYTICS / SOURCES sections, theme toggle, settings and the user chip.
 */
@Composable
fun WorkspaceSidebar(
    activeRoute: AppRoute,
    pendingUpdatesCount: Int,
    user: UserProfile?,
    isDarkTheme: Boolean,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onNavigate: (AppRoute) -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Column(
        modifier = modifier
            .width(300.dp)
            .fillMaxHeight()
            .background(colors.surface),
    ) {
        SidebarHeader()
        Box(modifier = Modifier.padding(ShellSpacing.sm)) {
            ShellSearchField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = SEARCH_TXT,
                trailing = { ShellKbdHint(text = SEARCH_SHORTCUT) },
            )
        }
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = ShellSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(ShellSpacing.xxs),
        ) {
            ShellSectionLabel(
                text = "Knowledge",
                modifier = Modifier.padding(start = ShellSpacing.sm, top = ShellSpacing.sm, bottom = ShellSpacing.xs),
            )
            SidebarItem(IconMessageSquare, AppRoute.ASSISTANT, activeRoute, onNavigate)
            SidebarItem(IconFileText, AppRoute.DOCUMENTS, activeRoute, onNavigate)
            SidebarItem(
                icon = IconAlertTriangle,
                route = AppRoute.UPDATES,
                activeRoute = activeRoute,
                onNavigate = onNavigate,
                badgeCount = pendingUpdatesCount,
            )

            ShellSectionLabel(
                text = "Analytics",
                modifier = Modifier.padding(start = ShellSpacing.sm, top = ShellSpacing.lg, bottom = ShellSpacing.xs),
            )
            SidebarItem(IconLayoutGrid, AppRoute.DASHBOARD, activeRoute, onNavigate)

            ShellSectionLabel(
                text = "Sources",
                modifier = Modifier.padding(start = ShellSpacing.sm, top = ShellSpacing.lg, bottom = ShellSpacing.xs),
            )
            SidebarSourceItem(IconLayers, CONFLUENCE_TXT, activeRoute, onNavigate)
        }
        Column(modifier = Modifier.padding(ShellSpacing.sm)) {
            SidebarActionRow(
                icon = if (isDarkTheme) IconSun else IconMoon,
                label = if (isDarkTheme) LIGHT_MODE_TXT else DARK_MODE_TXT,
                onClick = onToggleTheme,
            )
            SidebarItem(IconSettings, AppRoute.SETTINGS, activeRoute, onNavigate)
            if (user != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = ShellSpacing.xs)
                        .clip(RoundedCornerShape(ShellRadius.sm))
                        .background(colors.surfaceSubtle)
                        .padding(horizontal = ShellSpacing.sm, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
                ) {
                    ShellAvatar(initials = user.initials, size = 21.dp)
                    Column {
                        Text(user.fullName, style = ShellTheme.typography.label, color = colors.textPrimary)
                        Text(user.team, style = ShellTheme.typography.caption, color = colors.textMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarHeader() {
    val colors = ShellTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = ShellSpacing.md, bottom = ShellSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ShellSpacing.xs),
    ) {
        ShellBrandBadge(size = 44.dp, iconSize = 26.dp)
        Text(APP_BRAND_NAME, style = ShellTheme.typography.bodyStrong, color = colors.textPrimary)
    }
}

@Composable
private fun SidebarItem(
    icon: ImageVector,
    route: AppRoute,
    activeRoute: AppRoute,
    onNavigate: (AppRoute) -> Unit,
    badgeCount: Int = 0,
) {
    val colors = ShellTheme.colors
    val isActive = route == activeRoute
    val background by animateColorAsState(
        targetValue = if (isActive) colors.surfaceSelected else colors.surface,
        animationSpec = tween(ShellMotion.durationMedium),
        label = "sidebarItemBackground",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isActive) colors.brand else colors.textSecondary,
        animationSpec = tween(ShellMotion.durationMedium),
        label = "sidebarItemContent",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(RoundedCornerShape(ShellRadius.sm))
            .background(background)
            .testTag(DemoTestTags.navRoute(route.title))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onNavigate(route) }
            .padding(horizontal = ShellSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm + 1.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = route.title,
            style = if (isActive) ShellTheme.typography.bodyStrong else ShellTheme.typography.body,
            color = contentColor,
            modifier = Modifier.weight(1f),
        )
        if (badgeCount > 0) {
            ShellBadge(
                text = "$badgeCount",
                contentColor = colors.warning,
                containerColor = colors.warningSoft,
            )
        }
    }
}

@Composable
private fun SidebarSourceItem(
    icon: ImageVector,
    label: String,
    activeRoute: AppRoute,
    onNavigate: (AppRoute) -> Unit,
) {
    val colors = ShellTheme.colors
    val isActive = activeRoute == AppRoute.SOURCES
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(RoundedCornerShape(ShellRadius.sm))
            .testTag(DemoTestTags.navRoute(AppRoute.SOURCES.title))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onNavigate(AppRoute.SOURCES) }
            .padding(horizontal = ShellSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm + 1.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) colors.brand else colors.textMuted,
            modifier = Modifier.size(15.dp),
        )
        Text(text = label, style = ShellTheme.typography.body, color = colors.textSecondary)
    }
}

@Composable
private fun SidebarActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val colors = ShellTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(RoundedCornerShape(ShellRadius.sm))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = ShellSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm + 1.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(15.dp),
        )
        Text(text = label, style = ShellTheme.typography.bodyStrong, color = colors.textSecondary)
    }
}

/** Spacer alias kept for layout symmetry in previews. */
@Composable
fun SidebarSpacer(height: Int) {
    Spacer(modifier = Modifier.height(height.dp))
}
