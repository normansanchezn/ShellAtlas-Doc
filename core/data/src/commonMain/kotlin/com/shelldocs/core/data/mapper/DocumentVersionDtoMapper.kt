package com.shelldocs.core.data.mapper

import com.shelldocs.core.data.network.dto.DocumentVersionDto
import com.shelldocs.core.domain.entity.document.DocumentVersion
import kotlin.time.ExperimentalTime

object DocumentVersionDtoMapper {

    @OptIn(ExperimentalTime::class)
    fun toDomain(dto: DocumentVersionDto): DocumentVersion = DocumentVersion(
        id = dto.id,
        documentId = dto.documentId,
        versionNumber = dto.versionNumber,
        title = dto.title,
        rawMarkdown = dto.rawMarkdown,
        changeSummary = dto.changeSummary,
        createdAt = runCatching { kotlin.time.Instant.parse(dto.createdAt) }
            .getOrElse { kotlin.time.Instant.fromEpochMilliseconds(0) },
    )
}
