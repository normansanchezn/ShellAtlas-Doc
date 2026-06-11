package com.shelldocs.core.data.mapper

import com.shelldocs.core.data.network.dto.DocumentDto
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.DocumentAttributes
import com.shelldocs.core.domain.entity.document.DocumentClassification
import com.shelldocs.core.domain.entity.document.DocumentContent
import com.shelldocs.core.domain.entity.document.DocumentStatus
import kotlinx.datetime.Instant

object DocumentDtoMapper {

    fun toDomain(dto: DocumentDto): Document = Document(
        id = dto.id,
        title = dto.title,
        summary = dto.summary,
        status = DocumentStatus.fromKey(dto.status),
        classification = DocumentClassification.fromKey(dto.classification),
        rawMarkdown = dto.rawMarkdown,
        content = DocumentContent(
            schemaVersion = dto.contentJson.schemaVersion,
            blocks = dto.contentJson.blocks.map(ContentBlockDtoMapper::toDomain),
        ),
        plainText = dto.contentPlaintext,
        attributes = DocumentAttributes(
            owner = dto.attributes.owner,
            module = dto.attributes.module,
            team = dto.attributes.team,
            platform = dto.attributes.platform,
            parentFolderId = dto.attributes.parentFolderId,
            tags = dto.attributes.tags,
        ),
        createdAt = parseInstant(dto.createdAt),
        updatedAt = parseInstant(dto.updatedAt),
    )

    private fun parseInstant(raw: String): Instant =
        runCatching { Instant.parse(raw) }.getOrElse { Instant.fromEpochMilliseconds(0) }
}
