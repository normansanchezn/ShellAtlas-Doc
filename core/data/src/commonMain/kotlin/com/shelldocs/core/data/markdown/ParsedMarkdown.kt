package com.shelldocs.core.data.markdown

import com.shelldocs.core.domain.entity.document.DocumentContent

/** Output of [MarkdownParser]: structured blocks + search text + fingerprint. */
data class ParsedMarkdown(
    val content: DocumentContent,
    val plainText: String,
    val contentHash: String,
)
