package com.shelldocs.core.data.mapper

import com.shelldocs.core.data.network.dto.ContentBlockDto
import com.shelldocs.core.domain.entity.document.CodeBlock
import com.shelldocs.core.domain.entity.document.ContentBlock
import com.shelldocs.core.domain.entity.document.HeadingBlock
import com.shelldocs.core.domain.entity.document.ListBlock
import com.shelldocs.core.domain.entity.document.ListStyle
import com.shelldocs.core.domain.entity.document.ParagraphBlock
import com.shelldocs.core.domain.entity.document.QuoteBlock

/** Maps wire blocks to domain blocks; unknown types degrade to paragraphs. */
object ContentBlockDtoMapper {

    fun toDomain(dto: ContentBlockDto): ContentBlock = when (dto.type) {
        "heading" -> HeadingBlock(level = (dto.level ?: 1).coerceIn(1, 6), text = dto.text.orEmpty())
        "list" -> ListBlock(
            style = if (dto.style == "ordered") ListStyle.ORDERED else ListStyle.BULLET,
            items = dto.items.orEmpty(),
        )
        "code" -> CodeBlock(language = dto.language.orEmpty(), code = dto.code.orEmpty())
        "blockquote" -> QuoteBlock(text = dto.text.orEmpty())
        else -> ParagraphBlock(text = dto.text.orEmpty())
    }

    fun toDto(block: ContentBlock): ContentBlockDto = when (block) {
        is HeadingBlock -> ContentBlockDto(type = "heading", level = block.level, text = block.text)
        is ParagraphBlock -> ContentBlockDto(type = "paragraph", text = block.text)
        is ListBlock -> ContentBlockDto(
            type = "list",
            style = if (block.style == ListStyle.ORDERED) "ordered" else "bullet",
            items = block.items,
        )
        is CodeBlock -> ContentBlockDto(type = "code", language = block.language, code = block.code)
        is QuoteBlock -> ContentBlockDto(type = "blockquote", text = block.text)
    }
}
