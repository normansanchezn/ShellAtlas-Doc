package com.shelldocs.core.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shelldocs.core.designsystem.theme.ShellTheme

@Composable
fun ShellLoadingOverlay(
    message: String,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background.copy(alpha = 0.72f)),
            contentAlignment = Alignment.Center,
        ) {
            ShellFeedbackCard(modifier = modifier) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ShellLottieLoader(modifier = Modifier.size(72.dp))
                    Text(
                        text = message,
                        style = ShellTheme.typography.bodyStrong,
                        color = colors.textPrimary,
                    )
                }
            }
        }
    }
}
