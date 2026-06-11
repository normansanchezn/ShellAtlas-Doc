package com.shelldocs.core.data.markdown

import com.shelldocs.core.domain.entity.document.CodeBlock
import com.shelldocs.core.domain.entity.document.HeadingBlock
import com.shelldocs.core.domain.entity.document.ListBlock
import com.shelldocs.core.domain.entity.document.ListStyle
import com.shelldocs.core.domain.entity.document.ParagraphBlock
import com.shelldocs.core.domain.entity.document.QuoteBlock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MarkdownParserTest {

    private val parser = MarkdownParser()

    @Test
    fun parsesHeadingsWithLevels() {
        val blocks = parser.parse("# Title\n\n### Sub").content.blocks

        assertEquals(2, blocks.size)
        assertEquals(HeadingBlock(1, "Title"), blocks[0])
        assertEquals(HeadingBlock(3, "Sub"), blocks[1])
    }

    @Test
    fun groupsConsecutiveLinesIntoOneParagraph() {
        val blocks = parser.parse("First line\nsecond line\n\nNext paragraph").content.blocks

        assertEquals(2, blocks.size)
        assertEquals(ParagraphBlock("First line second line"), blocks[0])
        assertEquals(ParagraphBlock("Next paragraph"), blocks[1])
    }

    @Test
    fun parsesBulletAndOrderedLists() {
        val blocks = parser.parse("- one\n- two\n\n1. first\n2. second").content.blocks

        assertEquals(ListBlock(ListStyle.BULLET, listOf("one", "two")), blocks[0])
        assertEquals(ListBlock(ListStyle.ORDERED, listOf("first", "second")), blocks[1])
    }

    @Test
    fun parsesFencedCodeWithLanguage() {
        val blocks = parser.parse("```kotlin\nval x = 1\nprintln(x)\n```").content.blocks

        val code = assertIs<CodeBlock>(blocks.single())
        assertEquals("kotlin", code.language)
        assertEquals("val x = 1\nprintln(x)", code.code)
    }

    @Test
    fun parsesBlockQuotes() {
        val blocks = parser.parse("> wisdom\n> continued").content.blocks

        assertEquals(QuoteBlock("wisdom continued"), blocks.single())
    }

    @Test
    fun plainTextStripsInlineMarkers() {
        val parsed = parser.parse("# Title\n\nThis is **bold** and `code`.")

        assertTrue("**" !in parsed.plainText)
        assertTrue("`" !in parsed.plainText)
        assertTrue("bold" in parsed.plainText)
    }

    @Test
    fun hashIsStableForSameContentAndChangesWithContent() {
        val first = parser.parse("# Same")
        val second = parser.parse("# Same")
        val different = parser.parse("# Different")

        assertEquals(first.contentHash, second.contentHash)
        assertTrue(first.contentHash != different.contentHash)
    }

    @Test
    fun mixedDocumentKeepsBlockOrder() {
        val markdown = """
            # Release Process

            The release train ships at sprint end.

            ## QA Sign-off

            - Full regression suite
            - Physical devices

            ```bash
            ./release.sh
            ```
        """.trimIndent()

        val blocks = parser.parse(markdown).content.blocks
        assertIs<HeadingBlock>(blocks[0])
        assertIs<ParagraphBlock>(blocks[1])
        assertIs<HeadingBlock>(blocks[2])
        assertIs<ListBlock>(blocks[3])
        assertIs<CodeBlock>(blocks[4])
    }
}
