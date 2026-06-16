package com.shelldocs.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import com.shelldocs.core.common.testing.DemoTestTags
import com.shelldocs.core.designsystem.atoms.ShellBadge
import com.shelldocs.core.designsystem.icons.IconAlertTriangle
import com.shelldocs.core.designsystem.icons.IconFileText
import com.shelldocs.core.designsystem.icons.IconLayers
import com.shelldocs.core.designsystem.icons.IconLayoutGrid
import com.shelldocs.core.designsystem.icons.IconMessageSquare
import com.shelldocs.core.designsystem.icons.IconMoon
import com.shelldocs.core.designsystem.icons.IconSettings
import com.shelldocs.core.designsystem.icons.IconShellPecten
import com.shelldocs.core.designsystem.icons.IconSun
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellMotion
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing

/**
 * 72dp icon-only navigation rail for tablet widths — between the phone
 * bottom bar ([COMPACT_LAYOUT_MAX_WIDTH_DP]) and the full desktop sidebar
 * ([WIDE_LAYOUT_MIN_WIDTH_DP]).
 */
@Composable
fun WorkspaceRail(
    activeRoute: AppRoute,
    pendingUpdatesCount: Int,
    isDarkTheme: Boolean,
    onNavigate: (AppRoute) -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Column(
        modifier = modifier
            .width(72.dp)
            .fillMaxHeight()
            .background(colors.surface)
            .padding(vertical = ShellSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(ShellRadius.sm))
                .background(colors.brand),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = IconShellPecten,
                contentDescription = "ShellAtlas",
                tint = colors.onBrand,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f).padding(top = ShellSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(ShellSpacing.xs),
        ) {
            RailItem(IconMessageSquare, AppRoute.ASSISTANT, activeRoute, onNavigate)
            RailItem(IconFileText, AppRoute.DOCUMENTS, activeRoute, onNavigate)
            RailItem(IconAlertTriangle, AppRoute.UPDATES, activeRoute, onNavigate, badgeCount = pendingUpdatesCount)
            RailItem(IconLayoutGrid, AppRoute.DASHBOARD, activeRoute, onNavigate)
            RailItem(IconLayers, AppRoute.SOURCES, activeRoute, onNavigate)
        }
        Column(verticalArrangement = Arrangement.spacedBy(ShellSpacing.xs)) {
            RailActionItem(icon = if (isDarkTheme) IconSun else IconMoon, onClick = onToggleTheme)
            RailItem(IconSettings, AppRoute.SETTINGS, activeRoute, onNavigate)
        }
    }
}

@Composable
private fun RailItem(
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
        label = "railItemBackground",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isActive) colors.brand else colors.textMuted,
        animationSpec = tween(ShellMotion.durationMedium),
        label = "railItemContent",
    )
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(ShellRadius.lg))
            .background(background)
            .testTag(DemoTestTags.navRoute(route.title))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onNavigate(route) },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = route.title,
            tint = contentColor,
            modifier = Modifier.size(18.dp),
        )
        if (badgeCount > 0) {
            ShellBadge(
                text = "$badgeCount",
                contentColor = colors.warning,
                containerColor = colors.warningSoft,
                modifier = Modifier.padding(start = 30.dp, bottom = 26.dp),
            )
        }
    }
}

@Composable
private fun RailActionItem(icon: ImageVector, onClick: () -> Unit) {
    val colors = ShellTheme.colors
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(ShellRadius.lg))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(18.dp),
        )
    }
}
