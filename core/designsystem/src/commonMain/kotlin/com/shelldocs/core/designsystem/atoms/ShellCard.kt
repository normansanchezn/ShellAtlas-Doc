package com.shelldocs.core.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius

/** Bordered surface used for every panel and metric card. */
@Composable
fun ShellCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = ShellTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(ShellRadius.md))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(ShellRadius.md)),
    ) {
        content()
    }
}
