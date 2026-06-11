package com.shelldocs.core.designsystem.molecules

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/** Document-status donut: stroked arcs with 2-degree gaps. */
@Composable
fun ShellDonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 14.dp.toPx()
        val inset = strokeWidth / 2
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val gapDegrees = 2f
        var startAngle = -90f
        val visible = slices.filter { it.fraction > 0f }
        visible.forEach { slice ->
            val sweep = (slice.fraction * 360f - gapDegrees).coerceAtLeast(1f)
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidth),
            )
            startAngle += slice.fraction * 360f
        }
    }
}
