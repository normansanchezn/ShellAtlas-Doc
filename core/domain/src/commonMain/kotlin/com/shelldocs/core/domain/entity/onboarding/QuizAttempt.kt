package com.shelldocs.core.domain.entity.onboarding

import kotlin.time.ExperimentalTime

/** Graded outcome of submitting a [QuizQuestion] set for one checkpoint. Stored so it can be reviewed later. */
data class QuizAttempt @OptIn(ExperimentalTime::class) constructor(
    val checkpointId: String,
    val correct: Int,
    val total: Int,
    val submittedAt: kotlin.time.Instant,
) {
    val scorePercent: Int get() = if (total == 0) 0 else (correct * 100) / total
    val passed: Boolean get() = scorePercent >= PASSING_SCORE

    companion object {
        const val PASSING_SCORE = 80
    }
}
