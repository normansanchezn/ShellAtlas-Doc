package com.shelldocs.core.data.mapper

import com.shelldocs.core.data.network.dto.ContentBlockDto
import com.shelldocs.core.data.network.dto.ContentJsonDto
import com.shelldocs.core.data.network.dto.DocumentAttributesDto
import com.shelldocs.core.data.network.dto.DocumentDto
import com.shelldocs.core.domain.entity.document.DocumentStatus
import com.shelldocs.core.domain.entity.document.HeadingBlock
import com.shelldocs.core.domain.entity.document.ListBlock
import com.shelldocs.core.domain.entity.document.ListStyle
import com.shelldocs.core.domain.entity.document.ParagraphBlock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class DocumentDtoMapperTest {

    @Test
    fun mapsStatusBlocksAttributesAndDates() {
        val dto = DocumentDto(
            id = "doc-1",
            title = "Authentication",
            summary = "Tokens",
            status = "updates_pending",
            rawMarkdown = "# Authentication",
            contentJson = ContentJsonDto(
                blocks = listOf(
                    ContentBlockDto(type = "heading", level = 2, text = "Tokens"),
                    ContentBlockDto(type = "list", style = "ordered", items = listOf("a", "b")),
                    ContentBlockDto(type = "mystery", text = "fallback"),
                ),
            ),
            contentPlaintext = "Tokens",
            attributes = DocumentAttributesDto(owner = "Elena", module = "Auth", tags = listOf("auth")),
            createdAt = "2026-06-01T00:00:00Z",
            updatedAt = "2026-06-08T09:00:00Z",
        )

        val document = DocumentDtoMapper.toDomain(dto)

        assertEquals(DocumentStatus.UPDATES_PENDING, document.status)
        assertEquals(HeadingBlock(2, "Tokens"), document.content.blocks[0])
        assertEquals(ListBlock(ListStyle.ORDERED, listOf("a", "b")), document.content.blocks[1])
        assertEquals(ParagraphBlock("fallback"), document.content.blocks[2])
        assertEquals("Elena", document.attributes.owner)
        assertEquals(Instant.parse("2026-06-08T09:00:00Z"), document.updatedAt)
    }

    @Test
    fun invalidDatesFallBackToEpochInsteadOfCrashing() {
        val dto = DocumentDto(
            id = "doc-1",
            title = "X",
            createdAt = "not-a-date",
            updatedAt = "also-not-a-date",
        )

        val document = DocumentDtoMapper.toDomain(dto)

        assertEquals(Instant.fromEpochMilliseconds(0), document.createdAt)
    }
}
