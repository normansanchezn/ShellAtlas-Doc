package com.shelldocs.core.designsystem.tokens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing

/** Durations (ms) and easings shared by every interactive component. */
object ShellMotion {
    const val durationFast = 100
    const val durationMedium = 180
    const val durationSlow = 280

    val standard: Easing = FastOutSlowInEasing

    /** Material 3 "emphasized" decelerate — used for dialogs and sheets entering. */
    val emphasized: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

    /** Scale applied to pressable surfaces (cards, buttons) while pressed. */
    const val pressedScale = 0.97f
}
