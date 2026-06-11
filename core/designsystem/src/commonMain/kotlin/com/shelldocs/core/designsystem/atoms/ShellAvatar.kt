package com.shelldocs.core.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shelldocs.core.designsystem.theme.ShellTheme

/** Initials avatar; the current user gets the brand-yellow chip. */
@Composable
fun ShellAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    color: Color = ShellTheme.colors.brand,
    contentColor: Color = ShellTheme.colors.onBrand,
) {
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = TextStyle(fontSize = (size.value * 0.42f).sp, fontWeight = FontWeight.SemiBold),
            color = contentColor,
        )
    }
}
