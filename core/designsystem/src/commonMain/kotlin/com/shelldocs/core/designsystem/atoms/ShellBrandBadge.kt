package com.shelldocs.core.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.icons.IconShellAtlasBrand
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius

@Composable
fun ShellBrandBadge(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 24.dp,
) {
    val colors = ShellTheme.colors
    val shape = RoundedCornerShape(ShellRadius.md)
    Box(
        modifier = modifier
            .size(size)
            .background(
                color = colors.surface.copy(alpha = if (colors.isDark) 0.96f else 0.9f),
                shape = shape,
            )
            .border(
                width = 1.dp,
                color = colors.borderStrong.copy(alpha = if (colors.isDark) 0.78f else 0.54f),
                shape = shape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = IconShellAtlasBrand,
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(iconSize * 1.18f),
        )
    }
}
