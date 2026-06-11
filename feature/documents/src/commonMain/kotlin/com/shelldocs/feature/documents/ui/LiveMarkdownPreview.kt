package com.shelldocs.feature.documents.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shelldocs.core.domain.entity.document.ContentBlock
import com.shelldocs.core.domain.entity.document.HeadingBlock
import com.shelldocs.core.domain.entity.document.ListBlock
import com.shelldocs.core.domain.entity.document.ListStyle
import com.shelldocs.core.domain.entity.document.ParagraphBlock
import com.shelldocs.core.domain.entity.document.QuoteBlock
import com.shelldocs.core.domain.entity.document.CodeBlock

/**
 * Lightweight live preview of the editor content. It re-parses the raw
 * Markdown on every keystroke; parsing is line-based and cheap.
 */
@Composable
fun LiveMarkdownPreview(markdown: String, modifier: Modifier = Modifier) {
    val blocks = remember(markdown) { parsePreviewBlocks(markdown) }
    MarkdownBlocksView(blocks = blocks, modifier = modifier)
}

private val HEADING = Regex("^(#{1,6})\\s+(.*)$")
private val BULLET = Regex("^[-*+]\\s+(.*)$")
private val ORDERED = Regex("^\\d+[.)]\\s+(.*)$")

/** Minimal block parser for preview (full parser lives in core:data). */
internal fun parsePreviewBlocks(markdown: String): List<ContentBlock> {
    val blocks = mutableListOf<ContentBlock>()
    val lines = markdown.lines()
    var index = 0
    while (index < lines.size) {
        val trimmed = lines[index].trim()
        when {
            trimmed.isEmpty() -> index++
            trimmed.startsWith("```") -> {
                val language = trimmed.removePrefix("```").trim()
                val code = mutableListOf<String>()
                index++
                while (index < lines.size && !lines[index].trim().startsWith("```")) {
                    code += lines[index]; index++
                }
                index++
                blocks += CodeBlock(language, code.joinToString("\n"))
            }
            HEADING.matches(trimmed) -> {
                val match = HEADING.find(trimmed)!!
                blocks += HeadingBlock(match.groupValues[1].length, match.groupValues[2])
                index++
            }
            trimmed.startsWith(">") -> {
                blocks += QuoteBlock(trimmed.removePrefix(">").trim())
                index++
            }
            BULLET.matches(trimmed) -> {
                val items = mutableListOf<String>()
                while (index < lines.size && BULLET.matches(lines[index].trim())) {
                    items += BULLET.find(lines[index].trim())!!.groupValues[1]; index++
                }
                blocks += ListBlock(ListStyle.BULLET, items)
            }
            ORDERED.matches(trimmed) -> {
                val items = mutableListOf<String>()
                while (index < lines.size && ORDERED.matches(lines[index].trim())) {
                    items += ORDERED.find(lines[index].trim())!!.groupValues[1]; index++
                }
                blocks += ListBlock(ListStyle.ORDERED, items)
            }
            else -> {
                blocks += ParagraphBlock(trimmed)
                index++
            }
        }
    }
    return blocks
}
