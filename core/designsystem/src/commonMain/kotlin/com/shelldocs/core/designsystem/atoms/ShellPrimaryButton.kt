package com.shelldocs.core.designsystem.atoms

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellMotion
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing

/** Brand-yellow primary action ("Save Changes", "+ New", "Invite member"). */
@Composable
fun ShellPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val colors = ShellTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) ShellMotion.pressedScale else 1f,
        animationSpec = tween(ShellMotion.durationFast),
        label = "primaryButtonScale",
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.5f,
        animationSpec = tween(ShellMotion.durationMedium),
        label = "primaryButtonAlpha",
    )
    Box(
        modifier = modifier
            .alpha(contentAlpha)
            .scale(scale)
            .defaultMinSize(minWidth = 88.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(ShellRadius.sm))
            .background(colors.brand)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = colors.onBrand),
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = ShellSpacing.md),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.wrapContentWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.onBrand,
                    modifier = Modifier.size(13.dp),
                )
            }
            Text(
                text = text,
                style = ShellTheme.typography.label,
                color = colors.onBrand,
                modifier = Modifier.padding(start = if (icon != null) 6.dp else 0.dp),
            )
        }
    }
}
