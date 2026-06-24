package com.shelldocs.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shelldocs.app.navigation.AppRoute
import com.shelldocs.app.navigation.label
import com.shelldocs.core.common.testing.DemoTestTags
import com.shelldocs.core.designsystem.i18n.LocalAppStrings
import com.shelldocs.core.designsystem.icons.IconAlertTriangle
import com.shelldocs.core.designsystem.icons.IconFileText
import com.shelldocs.core.designsystem.icons.IconLayoutGrid
import com.shelldocs.core.designsystem.icons.IconMessageSquare
import com.shelldocs.core.designsystem.icons.IconSettings
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellElevation
import com.shelldocs.core.designsystem.tokens.ShellMotion
import com.shelldocs.core.designsystem.tokens.ShellRadius

/** Compact-layout bottom navigation (phones): the five core destinations. */
@Composable
fun WorkspaceBottomBar(
    activeRoute: AppRoute,
    onNavigate: (AppRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    val strings = LocalAppStrings.current
    val items: List<Pair<AppRoute, ImageVector>> = listOf(
        AppRoute.ASSISTANT to IconMessageSquare,
        AppRoute.DOCUMENTS to IconFileText,
        AppRoute.UPDATES to IconAlertTriangle,
        AppRoute.DASHBOARD to IconLayoutGrid,
        AppRoute.SETTINGS to IconSettings,
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = ShellElevation.raised)
            .height(64.dp)
            .background(colors.surface),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { (route, icon) ->
            val label = route.label(strings)
            val isActive = route == activeRoute
            // Selection background is a neutral tint (not the brand-amber surfaceSelected used by
            // risk/tab chips elsewhere) so the bottom bar's selected pill doesn't compete with or
            // get mistaken for an unrelated chip — only the icon/label color carries the brand cue.
            val indicatorColor by animateColorAsState(
                targetValue = if (isActive) colors.surfaceSubtle else colors.surface.copy(alpha = 0f),
                animationSpec = tween(ShellMotion.durationMedium),
                label = "bottomBarIndicator",
            )
            val contentColor by animateColorAsState(
                targetValue = if (isActive) colors.accentText else colors.textMuted,
                animationSpec = tween(ShellMotion.durationMedium),
                label = "bottomBarContent",
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag(DemoTestTags.navRoute(route.title))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                    ) { onNavigate(route) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Row(
                    modifier = Modifier
                        .height(24.dp)
                        .clip(RoundedCornerShape(ShellRadius.full))
                        .background(indicatorColor)
                        .padding(horizontal = ShellRadius.xl),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    text = label,
                    style = ShellTheme.typography.caption,
                    color = contentColor,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 2.dp),
                )
            }
        }
    }
}
