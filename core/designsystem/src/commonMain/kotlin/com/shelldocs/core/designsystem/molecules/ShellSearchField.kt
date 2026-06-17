package com.shelldocs.core.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.icons.IconSearch
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius

/** 28dp search input with leading icon, as in the sidebar and explorer. */
@Composable
fun ShellSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search docs...",
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = ShellTheme.colors
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }
    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(text = value, selection = TextRange(value.length))
        }
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(ShellRadius.sm))
            .background(colors.surfaceSubtle)
            .border(1.dp, colors.border, RoundedCornerShape(ShellRadius.sm))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Icon(
            imageVector = IconSearch,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(13.dp),
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                Text(text = placeholder, style = ShellTheme.typography.label, color = colors.textMuted)
            }
            BasicTextField(
                value = textFieldValue,
                onValueChange = { updated ->
                    textFieldValue = updated
                    onValueChange(updated.text)
                },
                singleLine = true,
                textStyle = ShellTheme.typography.label.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.brand),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        trailing?.invoke()
    }
}
