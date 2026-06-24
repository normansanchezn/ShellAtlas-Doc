package com.shelldocs.core.designsystem.modifier

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Clears focus (and runs [onTap]) when the user taps within this region, but only once the tap
 * reaches [PointerEventPass.Final] unconsumed — i.e. only if no button, field, or other clickable
 * underneath already handled it.
 *
 * Plain `detectTapGestures` runs on [PointerEventPass.Main], the *same* pass children's own
 * `clickable`/`combinedClickable` use. Two Main-pass gesture detectors racing for the same pointer
 * event on a wrapping container is what causes the "first click does nothing, the second one
 * works" symptom reported across the app (background tap-to-dismiss-keyboard wrapping a list of
 * clickable rows). Final pass runs strictly after Main, so this can only ever react to taps that
 * nothing else claimed — it can't ever steal a click meant for a child.
 */
fun Modifier.clearFocusOnOutsideTap(focusManager: FocusManager, onTap: () -> Unit = {}): Modifier =
    this.pointerInput(focusManager, onTap) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Final)
            val up = waitForUpOrCancellation(pass = PointerEventPass.Final)
            if (up != null && !up.isConsumed) {
                focusManager.clearFocus()
                onTap()
            }
        }
    }
