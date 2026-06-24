package com.shelldocs.core.designsystem.molecules

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellElevation
import com.shelldocs.core.designsystem.tokens.ShellRadius

/**
 * Right-click/long-press context menu styled to match [ShellCard][com.shelldocs.core.designsystem.atoms.ShellCard]
 * instead of the stock Material popup (which renders with Android's default surface/elevation on
 * desktop and web). Use for tree-node and table-row context actions.
 */
@Composable
fun ShellContextMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = ShellTheme.colors
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        shape = RoundedCornerShape(ShellRadius.md),
        containerColor = colors.surface,
        tonalElevation = 0.dp,
        shadowElevation = ShellElevation.overlay,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
        content = content,
    )
}

@Composable
fun ShellContextMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = ShellTheme.colors.textPrimary,
) {
    val colors = ShellTheme.colors
    DropdownMenuItem(
        text = { Text(text, style = ShellTheme.typography.body, color = tint) },
        onClick = onClick,
        modifier = modifier,
        leadingIcon = icon?.let { { Icon(imageVector = it, contentDescription = null, tint = tint) } },
        colors = MenuItemColors(
            textColor = tint,
            leadingIconColor = tint,
            trailingIconColor = tint,
            disabledTextColor = colors.textMuted,
            disabledLeadingIconColor = colors.textMuted,
            disabledTrailingIconColor = colors.textMuted,
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
    )
}
