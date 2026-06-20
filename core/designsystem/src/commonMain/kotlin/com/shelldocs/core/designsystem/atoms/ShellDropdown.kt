package com.shelldocs.core.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.icons.IconChevronDown
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing

/**
 * Fixed-choice selector rendered as an overlay popover (Compose's
 * [DropdownMenu] is anchored, never resizes the surrounding dialog/card, and
 * scrolls internally past [maxVisibleItems]). Use for any field that must
 * only ever hold one of a closed set of values — never free text.
 */
@Composable
fun <T> ShellDropdown(
    selected: T?,
    options: List<T>,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "Select...",
    maxVisibleItems: Int = 6,
) {
    val colors = ShellTheme.colors
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 32.dp)
                .clip(RoundedCornerShape(ShellRadius.sm))
                .background(colors.surfaceSubtle)
                .border(1.dp, colors.border, RoundedCornerShape(ShellRadius.sm))
                .clickable(enabled = enabled) { expanded = true }
                .padding(horizontal = ShellSpacing.md, vertical = ShellSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = selected?.let { label(it) } ?: placeholder,
                style = ShellTheme.typography.body,
                color = if (selected == null) colors.textMuted else colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = IconChevronDown,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(16.dp).rotate(if (expanded) 180f else 0f),
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            val itemHeight = 36.dp
            LazyColumn(modifier = Modifier.heightIn(max = itemHeight * maxVisibleItems)) {
                items(options) { option ->
                    DropdownMenuItem(
                        text = { Text(label(option), style = ShellTheme.typography.body) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}
