@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.core.domain.fixtures

import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.DocumentAttributes
import com.shelldocs.core.domain.entity.document.DocumentClassification
import com.shelldocs.core.domain.entity.document.DocumentContent
import com.shelldocs.core.domain.entity.document.DocumentStatus
import kotlinx.datetime.Instant

object DocumentFixtures {

    val baseInstant: Instant = Instant.parse("2026-06-01T00:00:00Z")

    fun document(
        id: String = "doc-1",
        title: String = "Authentication",
        summary: String = "iOS login, token refresh, and session recovery behavior.",
        status: DocumentStatus = DocumentStatus.PUBLISHED,
        markdown: String = "# Authentication\n\nAccess tokens expire after 60 minutes. " +
            "The client silently refreshes using the stored refresh token before prompting re-auth.",
        tags: List<String> = listOf("auth", "ios"),
        owner: String = "Elena Vargas",
        module: String = "Authentication",
        team: String = "iOS Shell App",
        platform: String = "iOS",
        updatedAt: Instant = baseInstant,
    ): Document = Document(
        id = id,
        title = title,
        summary = summary,
        status = status,
        classification = DocumentClassification.INTERNAL,
        rawMarkdown = markdown,
        content = DocumentContent(),
        plainText = markdown.replace(Regex("[#*`>]"), "").trim(),
        attributes = DocumentAttributes(
            owner = owner,
            module = module,
            team = team,
            platform = platform,
            tags = tags,
        ),
        createdAt = updatedAt,
        updatedAt = updatedAt,
    )
}
