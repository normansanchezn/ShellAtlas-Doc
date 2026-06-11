package com.shelldocs.feature.assistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.icons.IconSend
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing

/** Bottom ask bar: "Ask about local documentation…" + send action. */
@Composable
fun ChatInputBar(
    value: String,
    canSend: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Column(
        modifier = modifier.fillMaxWidth().padding(
            horizontal = ShellSpacing.xxl,
            vertical = ShellSpacing.md,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp)
                .clip(RoundedCornerShape(ShellRadius.lg))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(ShellRadius.lg))
                .padding(horizontal = ShellSpacing.lg, vertical = ShellSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ShellSpacing.md),
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = "Ask about local documentation...",
                        style = ShellTheme.typography.body,
                        color = colors.textMuted,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = ShellTheme.typography.body.copy(color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.brand),
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Icon(
                imageVector = IconSend,
                contentDescription = "Send",
                tint = if (canSend) colors.brand else colors.textMuted,
                modifier = Modifier
                    .size(16.dp)
                    .alpha(if (canSend) 1f else 0.6f)
                    .clickable(enabled = canSend, onClick = onSend),
            )
        }
        Text(
            text = "Answers are grounded on indexed documentation. Always verify critical information.",
            style = ShellTheme.typography.caption,
            color = colors.textMuted,
            modifier = Modifier.padding(top = ShellSpacing.xs),
        )
    }
}
