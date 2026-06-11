package com.shelldocs.feature.documents.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.theme.ShellTheme

/** Thin draggable divider between two side-by-side panels. */
@Composable
fun ResizeHandle(onDrag: (Float) -> Unit, modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(4.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x)
                }
            }
            .background(colors.border),
    )
}

/** Narrow rail shown instead of a collapsed panel, with a button to expand it again. */
@Composable
fun CollapsedPanelRail(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(28.dp)
            .background(colors.surface),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 12.dp)
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = contentDescription, tint = colors.textMuted)
        }
    }
}
