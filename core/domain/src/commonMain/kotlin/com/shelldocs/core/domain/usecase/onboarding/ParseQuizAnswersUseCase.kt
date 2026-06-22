package com.shelldocs.core.domain.usecase.onboarding

import com.shelldocs.core.domain.entity.onboarding.QuizQuestion

/**
 * Parses free-text replies like "1a 2c 3b" or "1) A, 2) C, 3) B" into question-id -> option-index
 * answers. Returns null (not partial results) unless every question in [questions] was answered,
 * so a stray reply mid-quiz falls back to normal chat instead of grading an incomplete attempt.
 */
class ParseQuizAnswersUseCase {

    operator fun invoke(message: String, questions: List<QuizQuestion>): Map<String, Int>? {
        val matches = PAIR_PATTERN.findAll(message.lowercase())
            .associate { it.groupValues[1].toInt() to (it.groupValues[2][0] - 'a') }
        if (matches.size < questions.size) return null
        val answers = mutableMapOf<String, Int>()
        questions.forEachIndexed { index, question ->
            val optionIndex = matches[index + 1] ?: return null
            if (optionIndex !in question.options.indices) return null
            answers[question.id] = optionIndex
        }
        return answers
    }

    private companion object {
        val PAIR_PATTERN = Regex("""(\d+)\D{0,3}([a-d])\b""")
    }
}
