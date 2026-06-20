package com.shelldocs.core.designsystem.molecules

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing

/**
 * Single continuous markdown editor — the canonical create/edit experience,
 * shared by every feature that lets a user author or revise a document's
 * raw markdown. Never split content into multiple fields; bind this to one
 * string for the whole document.
 */
@Composable
fun MarkdownEditorField(
    markdown: String,
    onMarkdownChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    showToolbar: Boolean = false,
) {
    val colors = ShellTheme.colors
    val scrollState = rememberScrollState()
    var isFocused by remember { mutableStateOf(false) }
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = markdown,
                selection = TextRange(markdown.length),
            ),
        )
    }

    LaunchedEffect(markdown) {
        if (markdown != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(
                text = markdown,
                selection = TextRange(markdown.length),
            )
        }
    }

    LaunchedEffect(textFieldValue.text, textFieldValue.selection, isFocused) {
        if (isFocused && textFieldValue.selection.end >= textFieldValue.text.length - 1) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ShellRadius.md))
            .background(colors.surfaceSubtle)
            .border(
                width = Dp.Hairline,
                color = colors.border,
                shape = RoundedCornerShape(ShellRadius.md),
            ),
    ) {
        if (showToolbar) {
            MarkdownToolbar(
                onAction = { action ->
                    val updated = applyMarkdownAction(action, textFieldValue)
                    textFieldValue = updated
                    onMarkdownChange(updated.text)
                },
            )
        }
        BasicTextField(
            value = textFieldValue,
            onValueChange = { updated ->
                textFieldValue = updated
                onMarkdownChange(updated.text)
            },
            textStyle = ShellTheme.typography.code.copy(color = colors.textPrimary),
            cursorBrush = SolidColor(colors.brand),
            modifier = fieldModifier
                .fillMaxWidth()
                .weight(1f)
                .onFocusChanged { isFocused = it.isFocused }
                .padding(ShellSpacing.lg)
                .verticalScroll(scrollState),
        )
    }
}

enum class MarkdownAction(val toolbarLabel: String, val contentDescription: String) {
    Bold("B", "Bold"),
    Italic("I", "Italic"),
    Heading1("H1", "Heading 1"),
    Heading2("H2", "Heading 2"),
    Heading3("H3", "Heading 3"),
    BulletList("•", "Bullet list"),
    Table("⊞", "Table"),
}

/** Pinned row of formatting shortcuts above the editor; stays put while the field scrolls. */
@Composable
private fun MarkdownToolbar(onAction: (MarkdownAction) -> Unit) {
    val colors = ShellTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .border(
                width = Dp.Hairline,
                color = colors.border,
            )
            .padding(horizontal = ShellSpacing.sm, vertical = ShellSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(ShellSpacing.xs),
    ) {
        MarkdownAction.entries.forEach { action ->
            MarkdownToolbarButton(action) { onAction(action) }
        }
    }
}

@Composable
private fun MarkdownToolbarButton(action: MarkdownAction, onClick: () -> Unit) {
    val colors = ShellTheme.colors
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 32.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(ShellRadius.sm))
            .clickable(onClick = onClick)
            .semantics { contentDescription = action.contentDescription }
            .padding(horizontal = ShellSpacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = action.toolbarLabel,
            style = ShellTheme.typography.label,
            color = colors.textSecondary,
        )
    }
}

private fun applyMarkdownAction(action: MarkdownAction, value: TextFieldValue): TextFieldValue = when (action) {
    MarkdownAction.Bold -> wrapSelection(value, "**", "**", "bold text")
    MarkdownAction.Italic -> wrapSelection(value, "_", "_", "italic text")
    MarkdownAction.Heading1 -> prefixLine(value, "# ")
    MarkdownAction.Heading2 -> prefixLine(value, "## ")
    MarkdownAction.Heading3 -> prefixLine(value, "### ")
    MarkdownAction.BulletList -> prefixLine(value, "- ")
    MarkdownAction.Table -> insertBlock(
        value,
        "\n| Column 1 | Column 2 |\n| --- | --- |\n| Cell | Cell |\n",
    )
}

private fun wrapSelection(value: TextFieldValue, prefix: String, suffix: String, placeholder: String): TextFieldValue {
    val text = value.text
    val start = value.selection.min
    val end = value.selection.max
    return if (start == end) {
        val newText = text.substring(0, start) + prefix + placeholder + suffix + text.substring(end)
        TextFieldValue(newText, TextRange(start + prefix.length, start + prefix.length + placeholder.length))
    } else {
        val selected = text.substring(start, end)
        val newText = text.substring(0, start) + prefix + selected + suffix + text.substring(end)
        TextFieldValue(newText, TextRange(start + prefix.length, end + prefix.length))
    }
}

private fun prefixLine(value: TextFieldValue, marker: String): TextFieldValue {
    val text = value.text
    val cursor = value.selection.min
    val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
    val newText = text.substring(0, lineStart) + marker + text.substring(lineStart)
    val newCursor = value.selection.max + marker.length
    return TextFieldValue(newText, TextRange(newCursor))
}

private fun insertBlock(value: TextFieldValue, block: String): TextFieldValue {
    val text = value.text
    val start = value.selection.min
    val end = value.selection.max
    val newText = text.substring(0, start) + block + text.substring(end)
    return TextFieldValue(newText, TextRange(start + block.length))
}
