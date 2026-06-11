package com.shelldocs.core.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing

@Composable
fun ShellLoadingOverlay(
    message: String,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(ShellRadius.lg))
                .background(colors.surface)
                .padding(horizontal = ShellSpacing.xl, vertical = ShellSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(color = colors.brand, strokeWidth = 3.dp)
            Text(
                text = message,
                style = ShellTheme.typography.bodyStrong,
                color = colors.textPrimary,
            )
        }
    }
}
