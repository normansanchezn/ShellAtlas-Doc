package com.shelldocs.core.data.mapper

import com.shelldocs.core.data.network.dto.DocumentDto
import com.shelldocs.core.domain.entity.document.*
import kotlin.time.ExperimentalTime

object DocumentDtoMapper {

    @OptIn(ExperimentalTime::class)
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
            area = Area.fromKey(dto.attributes.area),
            applicationVersion = dto.attributes.applicationVersion,
        ),
        createdAt = parseInstant(dto.createdAt),
        updatedAt = parseInstant(dto.updatedAt),
    )

    @OptIn(ExperimentalTime::class)
    private fun parseInstant(raw: String): kotlin.time.Instant =
        runCatching { kotlin.time.Instant.parse(raw) }.getOrElse { kotlin.time.Instant.fromEpochMilliseconds(0) }
}
