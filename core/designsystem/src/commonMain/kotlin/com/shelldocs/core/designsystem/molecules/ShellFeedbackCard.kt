package com.shelldocs.core.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing

@Composable
internal fun ShellFeedbackCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = ShellTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ShellRadius.lg))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(ShellRadius.lg))
            .padding(horizontal = ShellSpacing.xl, vertical = ShellSpacing.lg),
        content = content,
    )
}
