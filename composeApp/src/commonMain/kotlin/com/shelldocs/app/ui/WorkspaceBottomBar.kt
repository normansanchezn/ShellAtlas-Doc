package com.shelldocs.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.shelldocs.app.navigation.AppRoute
import com.shelldocs.core.designsystem.icons.IconAlertTriangle
import com.shelldocs.core.designsystem.icons.IconFileText
import com.shelldocs.core.designsystem.icons.IconLayoutGrid
import com.shelldocs.core.designsystem.icons.IconMessageSquare
import com.shelldocs.core.designsystem.icons.IconSettings
import com.shelldocs.core.designsystem.theme.ShellTheme

/** Compact-layout bottom navigation (phones): the five core destinations. */
@Composable
fun WorkspaceBottomBar(
    activeRoute: AppRoute,
    onNavigate: (AppRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
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
            .height(56.dp)
            .background(colors.surface),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { (route, icon) ->
            val isActive = route == activeRoute
            Column(
                modifier = Modifier
                    .clickable { onNavigate(route) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = route.title,
                    tint = if (isActive) colors.brand else colors.textMuted,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = route.title.substringBefore(' '),
                    style = ShellTheme.typography.caption,
                    color = if (isActive) colors.brand else colors.textMuted,
                )
            }
        }
    }
}
