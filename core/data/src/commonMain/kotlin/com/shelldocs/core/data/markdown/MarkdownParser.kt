package com.shelldocs.core.data.markdown

import com.shelldocs.core.domain.entity.document.*

/**
 * Kotlin port of the original backend `markdown-parser.ts`: converts raw
 * Markdown into semantic blocks (heading, paragraph, list, code, quote),
 * plus plain text for search and a content hash for deduplication.
 */
class MarkdownParser {

    fun parse(rawMarkdown: String): ParsedMarkdown {
        val blocks = parseBlocks(rawMarkdown)
        val plainText = blocks.joinToString("\n") { blockText(it) }.trim()
        return ParsedMarkdown(
            content = DocumentContent(schemaVersion = SCHEMA_VERSION, blocks = blocks),
            plainText = plainText,
            contentHash = ContentHasher.hash(rawMarkdown),
        )
    }

    private fun parseBlocks(rawMarkdown: String): List<ContentBlock> {
        val blocks = mutableListOf<ContentBlock>()
        val lines = rawMarkdown.lines()
        var index = 0

        while (index < lines.size) {
            val line = lines[index]
            val trimmed = line.trim()
            when {
                trimmed.isEmpty() -> index++

                isTableRow(trimmed) && index + 1 < lines.size && TABLE_SEPARATOR.matches(lines[index + 1].trim()) -> {
                    val headers = splitTableRow(trimmed)
                    index += 2 // header + separator
                    val rows = mutableListOf<List<String>>()
                    while (index < lines.size && isTableRow(lines[index].trim())) {
                        rows += splitTableRow(lines[index].trim())
                        index++
                    }
                    blocks += TableBlock(headers = headers, rows = rows)
                }

                trimmed.startsWith("```") -> {
                    val language = trimmed.removePrefix("```").trim()
                    val code = mutableListOf<String>()
                    index++
                    while (index < lines.size && !lines[index].trim().startsWith("```")) {
                        code += lines[index]
                        index++
                    }
                    index++ // closing fence
                    blocks += CodeBlock(language = language, code = code.joinToString("\n"))
                }

                HEADING.matches(trimmed) -> {
                    val match = HEADING.find(trimmed)!!
                    blocks += HeadingBlock(
                        level = match.groupValues[1].length,
                        text = match.groupValues[2].trim(),
                    )
                    index++
                }

                trimmed.startsWith("> ") || trimmed == ">" -> {
                    val quote = mutableListOf<String>()
                    while (index < lines.size && lines[index].trim().let { it.startsWith(">") }) {
                        quote += lines[index].trim().removePrefix(">").trim()
                        index++
                    }
                    blocks += QuoteBlock(text = quote.joinToString(" ").trim())
                }

                BULLET_ITEM.matches(trimmed) -> {
                    val items = mutableListOf<String>()
                    while (index < lines.size && BULLET_ITEM.matches(lines[index].trim())) {
                        items += BULLET_ITEM.find(lines[index].trim())!!.groupValues[1].trim()
                        index++
                    }
                    blocks += ListBlock(style = ListStyle.BULLET, items = items)
                }

                ORDERED_ITEM.matches(trimmed) -> {
                    val items = mutableListOf<String>()
                    while (index < lines.size && ORDERED_ITEM.matches(lines[index].trim())) {
                        items += ORDERED_ITEM.find(lines[index].trim())!!.groupValues[1].trim()
                        index++
                    }
                    blocks += ListBlock(style = ListStyle.ORDERED, items = items)
                }

                else -> {
                    val paragraph = mutableListOf<String>()
                    while (index < lines.size && lines[index].trim().isNotEmpty() &&
                        !isBlockStart(lines[index].trim())
                    ) {
                        paragraph += lines[index].trim()
                        index++
                    }
                    blocks += ParagraphBlock(text = paragraph.joinToString(" "))
                }
            }
        }
        return blocks
    }

    private fun isBlockStart(trimmed: String): Boolean =
        trimmed.startsWith("```") || trimmed.startsWith(">") || isTableRow(trimmed) ||
            HEADING.matches(trimmed) || BULLET_ITEM.matches(trimmed) || ORDERED_ITEM.matches(trimmed)

    private fun isTableRow(trimmed: String): Boolean =
        trimmed.startsWith("|") && trimmed.endsWith("|") && trimmed.length > 1

    private fun splitTableRow(trimmed: String): List<String> =
        trimmed.removePrefix("|").removeSuffix("|").split("|").map { it.trim() }

    private fun blockText(block: ContentBlock): String = when (block) {
        is HeadingBlock -> block.text
        is ParagraphBlock -> stripInline(block.text)
        is ListBlock -> block.items.joinToString("\n") { stripInline(it) }
        is CodeBlock -> block.code
        is QuoteBlock -> stripInline(block.text)
        is TableBlock -> (listOf(block.headers) + block.rows).joinToString("\n") { it.joinToString(" ") }
    }

    private fun stripInline(text: String): String =
        text.replace(INLINE_MARKERS, "")

    private companion object {
        const val SCHEMA_VERSION = 1
        val HEADING = Regex("^(#{1,6})\\s+(.*)$")
        val BULLET_ITEM = Regex("^[-*+]\\s+(.*)$")
        val ORDERED_ITEM = Regex("^\\d+[.)]\\s+(.*)$")
        val TABLE_SEPARATOR = Regex("^\\|?\\s*:?-+:?\\s*(\\|\\s*:?-+:?\\s*)*\\|?$")
        val INLINE_MARKERS = Regex("[*_`]")
    }
}
