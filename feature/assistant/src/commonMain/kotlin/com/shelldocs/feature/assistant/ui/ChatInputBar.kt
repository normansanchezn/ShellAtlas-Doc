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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.shelldocs.core.common.testing.DemoTestTags
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
    // Buffer the edit locally; feeding the StateFlow-backed `value` straight into
    // BasicTextField races the IME on fast typing and can scramble characters
    // when the round trip lags a frame behind a keystroke (see ShellTextField).
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }
    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(text = value, selection = TextRange(value.length))
        }
    }
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
                    value = textFieldValue,
                    onValueChange = { updated ->
                        textFieldValue = updated
                        onValueChange(updated.text)
                    },
                    textStyle = ShellTheme.typography.body.copy(color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.info),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = { if (canSend) onSend() },
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(DemoTestTags.AssistantInput)
                        .onPreviewKeyEvent { event ->
                            if (
                                event.type == KeyEventType.KeyUp &&
                                event.key == Key.Enter &&
                                !event.isShiftPressed
                            ) {
                                if (canSend) onSend()
                                true
                            } else {
                                false
                            }
                        },
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .alpha(if (canSend) 1f else 0.6f)
                    .clip(RoundedCornerShape(10.dp))
                    .testTag(DemoTestTags.AssistantSend)
                    .clickable(enabled = canSend, onClick = onSend),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = IconSend,
                    contentDescription = "Send",
                    tint = if (canSend) colors.brand else colors.textMuted,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Text(
            text = "Answers are grounded on indexed documentation. Always verify critical information.",
            style = ShellTheme.typography.caption,
            color = colors.textMuted,
            modifier = Modifier.padding(top = ShellSpacing.xs),
        )
    }
}
