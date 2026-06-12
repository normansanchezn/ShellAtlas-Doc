package com.shelldocs.core.designsystem.atoms

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellElevation
import com.shelldocs.core.designsystem.tokens.ShellMotion
import com.shelldocs.core.designsystem.tokens.ShellRadius

/**
 * Bordered surface used for every panel and metric card.
 *
 * Set [elevated] for cards that should float above the page (metric cards,
 * popovers). Pass [onClick] to make the whole card pressable — it gently
 * scales down while pressed.
 */
@Composable
fun ShellCard(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = ShellTheme.colors
    val shape = RoundedCornerShape(ShellRadius.md)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) ShellMotion.pressedScale else 1f,
        animationSpec = tween(ShellMotion.durationFast),
        label = "cardPressScale",
    )
    val elevation = if (elevated) ShellElevation.raised else ShellElevation.none

    var boxModifier = modifier
        .scale(if (onClick != null) scale else 1f)
        .shadow(elevation = elevation, shape = shape, clip = false)
        .clip(shape)
        .background(colors.surface)
        .border(1.dp, colors.border, shape)

    if (onClick != null) {
        boxModifier = boxModifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            role = Role.Button,
            onClick = onClick,
        )
    }

    Box(modifier = boxModifier) {
        content()
    }
}
