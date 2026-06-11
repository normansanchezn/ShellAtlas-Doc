package com.shelldocs.core.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.theme.ShellTheme

/** One bar of a [ShellBarChart]. */
data class BarEntry(val label: String, val value: Int, val highlighted: Boolean = false)

/** Weekly AI-usage bars; the peak day is highlighted in brand yellow. */
@Composable
fun ShellBarChart(
    entries: List<BarEntry>,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    val max = entries.maxOfOrNull { it.value }?.coerceAtLeast(1) ?: 1
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        entries.forEach { entry ->
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((entry.value.toFloat() / max * 88f).dp.coerceAtLeast(3.dp))
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(if (entry.highlighted) colors.brand else colors.surfaceSubtle),
                )
                Text(
                    text = entry.label,
                    style = ShellTheme.typography.caption,
                    color = colors.textMuted,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}
