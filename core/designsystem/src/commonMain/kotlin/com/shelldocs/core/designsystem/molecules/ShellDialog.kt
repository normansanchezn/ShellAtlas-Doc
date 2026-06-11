package com.shelldocs.core.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing

/** Centered modal dialog: title, content and a row of trailing actions. */
@Composable
fun ShellDialog(
    title: String,
    onDismiss: () -> Unit,
    actions: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = ShellTheme.colors
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = modifier
                .widthIn(min = 360.dp, max = 480.dp)
                .clip(RoundedCornerShape(ShellRadius.lg))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(ShellRadius.lg))
                .padding(ShellSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(ShellSpacing.md),
        ) {
            Text(text = title, style = ShellTheme.typography.sectionTitle, color = colors.textPrimary)
            content()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm, Alignment.End),
            ) {
                actions()
            }
        }
    }
}
