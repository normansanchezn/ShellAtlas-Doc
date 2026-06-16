package com.shelldocs.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.shelldocs.app.navigation.AppRoute
import com.shelldocs.core.designsystem.atoms.ShellAvatar
import com.shelldocs.core.designsystem.atoms.ShellBadge
import com.shelldocs.core.designsystem.atoms.ShellSectionLabel
import com.shelldocs.core.designsystem.icons.IconAlertTriangle
import com.shelldocs.core.designsystem.icons.IconBookOpen
import com.shelldocs.core.designsystem.icons.IconFileText
import com.shelldocs.core.designsystem.icons.IconGitBranch
import com.shelldocs.core.designsystem.icons.IconLayers
import com.shelldocs.core.designsystem.icons.IconLayoutGrid
import com.shelldocs.core.designsystem.icons.IconMessageSquare
import com.shelldocs.core.designsystem.icons.IconMoon
import com.shelldocs.core.designsystem.icons.IconSettings
import com.shelldocs.core.designsystem.icons.IconShellPecten
import com.shelldocs.core.designsystem.icons.IconSun
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
            .width(240.dp)
            .fillMaxHeight()
            .background(colors.surface),
    ) {
        SidebarHeader()
        Box(modifier = Modifier.padding(ShellSpacing.sm)) {
            ShellSearchField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = "Search docs...",
                trailing = { ShellKbdHint(text = "⌘ K") },
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
            SidebarSourceItem(IconLayers, "Confluence", activeRoute, onNavigate)
            SidebarSourceItem(IconGitBranch, "Azure DevOps", activeRoute, onNavigate)
            SidebarSourceItem(IconBookOpen, "Jira", activeRoute, onNavigate)
        }
        Column(modifier = Modifier.padding(ShellSpacing.sm)) {
            SidebarActionRow(
                icon = if (isDarkTheme) IconSun else IconMoon,
                label = if (isDarkTheme) "Light Mode" else "Dark Mode",
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(ShellRadius.sm))
                .background(colors.brand),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = IconShellPecten,
                contentDescription = "ShellAtlas",
                tint = colors.onBrand,
                modifier = Modifier.size(15.dp),
            )
        }
        Column {
            Text("ShellAtlas", style = ShellTheme.typography.bodyStrong, color = colors.textPrimary)
            Text("Knowledge Platform", style = ShellTheme.typography.caption, color = colors.textMuted)
        }
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
            .clickable { onNavigate(route) }
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
            .clickable { onNavigate(AppRoute.SOURCES) }
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
            .clickable(onClick = onClick)
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
