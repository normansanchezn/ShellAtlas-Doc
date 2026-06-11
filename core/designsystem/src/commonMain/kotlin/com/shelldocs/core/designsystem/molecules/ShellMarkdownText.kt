package com.shelldocs.core.designsystem.molecules

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/** Renders inline Markdown (`**bold**`, `` `code` ``, `*medium*`) as styled text. */
@Composable
fun ShellMarkdownText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(text = parseInlineMarkdown(text), style = style, color = color, modifier = modifier)
}

private val INLINE_PATTERN = Regex("(\\*\\*[^*]+\\*\\*|`[^`]+`|\\*[^*]+\\*)")

internal fun parseInlineMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    var cursor = 0
    INLINE_PATTERN.findAll(text).forEach { match ->
        append(text.substring(cursor, match.range.first))
        val token = match.value
        when {
            token.startsWith("**") -> withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                append(token.removeSurrounding("**"))
            }
            token.startsWith("`") -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                append(token.removeSurrounding("`"))
            }
            else -> withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                append(token.removeSurrounding("*"))
            }
        }
        cursor = match.range.last + 1
    }
    append(text.substring(cursor))
}
