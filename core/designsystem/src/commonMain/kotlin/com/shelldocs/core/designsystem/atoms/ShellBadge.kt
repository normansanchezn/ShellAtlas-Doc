package com.shelldocs.core.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius

/** Soft-tinted pill with strong-colored text (counters, role chips, risk). */
@Composable
fun ShellBadge(
    text: String,
    contentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = ShellTheme.typography.caption,
        color = contentColor,
        modifier = modifier
            .clip(RoundedCornerShape(ShellRadius.full))
            .background(containerColor)
            .padding(horizontal = 7.dp, vertical = 2.dp),
    )
}
