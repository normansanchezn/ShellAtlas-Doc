package com.shelldocs.core.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing

/** Centered placeholder ("Select a document to read"). */
@Composable
fun ShellEmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val colors = ShellTheme.colors
    ShellFeedbackCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(colors.surfaceSubtle),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = title,
                style = ShellTheme.typography.bodyStrong,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = ShellSpacing.md),
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = ShellTheme.typography.caption,
                    color = colors.textMuted,
                    modifier = Modifier.padding(top = ShellSpacing.xs),
                )
            }
        }
    }
}
