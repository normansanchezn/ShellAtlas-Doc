package com.shelldocs.core.data.mapper

import com.shelldocs.core.data.network.dto.DocumentVersionDto
import com.shelldocs.core.domain.entity.document.DocumentVersion
import kotlinx.datetime.Instant

object DocumentVersionDtoMapper {

    fun toDomain(dto: DocumentVersionDto): DocumentVersion = DocumentVersion(
        id = dto.id,
        documentId = dto.documentId,
        versionNumber = dto.versionNumber,
        title = dto.title,
        rawMarkdown = dto.rawMarkdown,
        changeSummary = dto.changeSummary,
        createdAt = runCatching { Instant.parse(dto.createdAt) }
            .getOrElse { Instant.fromEpochMilliseconds(0) },
    )
}
