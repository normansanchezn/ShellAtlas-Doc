package com.shelldocs.core.domain.usecase.assistant

import com.shelldocs.core.domain.entity.document.Document

/** Retrieval result: a candidate grounding document and its relevance (0..1+). */
data class ScoredDocument(
    val document: Document,
    val score: Double,
)
