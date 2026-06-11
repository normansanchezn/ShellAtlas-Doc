package com.shelldocs.core.domain.entity.assistant

import com.shelldocs.core.domain.entity.document.Document

/** Retrieval result: a candidate grounding document and its relevance (0..1+). */
data class ScoredDocument(
    val document: Document,
    val score: Double,
) {
    /** Relevance as shown next to each source citation (e.g. "97%"). */
    val relevancePercent: Int = (score * 100).toInt().coerceIn(1, 99)
}
