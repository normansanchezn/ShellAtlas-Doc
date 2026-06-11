package com.shelldocs.core.designsystem.molecules

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.theme.ShellTheme

/** 34x18 switch matching the Figma toggles (yellow when on). */
@Composable
fun ShellToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    val track by animateColorAsState(if (checked) colors.brand else colors.borderStrong)
    val thumbOffset by animateDpAsState(if (checked) 16.dp else 0.dp)
    Box(
        modifier = modifier
            .size(width = 34.dp, height = 18.dp)
            .clip(CircleShape)
            .background(track)
            .clickable { onCheckedChange(!checked) }
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(14.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}
