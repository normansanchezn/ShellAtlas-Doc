package com.shelldocs.core.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing

/** Dashboard stat card: tinted icon chip, big value, caption, optional delta. */
@Composable
fun ShellMetricCard(
    icon: ImageVector,
    iconTint: Color,
    value: String,
    caption: String,
    modifier: Modifier = Modifier,
    delta: String? = null,
    deltaColor: Color = ShellTheme.colors.success,
) {
    val colors = ShellTheme.colors
    ShellCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(ShellSpacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(ShellRadius.sm))
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(14.dp),
                    )
                }
                if (delta != null) {
                    Text(text = delta, style = ShellTheme.typography.caption, color = deltaColor)
                }
            }
            Text(
                text = value,
                style = ShellTheme.typography.metricValue,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = ShellSpacing.md),
            )
            Text(text = caption, style = ShellTheme.typography.caption, color = colors.textMuted)
        }
    }
}
