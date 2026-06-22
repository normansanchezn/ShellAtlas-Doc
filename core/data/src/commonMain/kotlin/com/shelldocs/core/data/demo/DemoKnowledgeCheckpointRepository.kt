@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shelldocs.core.data.demo

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.domain.entity.onboarding.KnowledgeCheckpoint
import com.shelldocs.core.domain.entity.onboarding.KnowledgeProgress
import com.shelldocs.core.domain.entity.onboarding.QuizAttempt
import com.shelldocs.core.domain.entity.onboarding.QuizQuestion
import com.shelldocs.core.domain.repository.KnowledgeCheckpointRepository
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory guided knowledge-transfer checklist for the current session. */
class DemoKnowledgeCheckpointRepository(private val timeProvider: TimeProvider) : KnowledgeCheckpointRepository {

    private val items: List<KnowledgeCheckpoint> = listOf(
        KnowledgeCheckpoint(
            id = "checkpoint-welcome",
            order = 1,
            title = "Welcome to Shell",
            instruction = "Review the \"Welcome to Shell\" document in Documents > Onboarding to meet the team, learn where documentation lives, and how this assistant works. When you're done, tell me \"done\".",
            documentId = "doc-onboarding-welcome",
        ),
        KnowledgeCheckpoint(
            id = "checkpoint-android-setup",
            order = 2,
            title = "Set up your Android environment",
            instruction = "Review \"Android Developer Setup\" in Documents > Onboarding to clone the repository and prepare your machine (values are MOCK). When you're done, tell me \"done\".",
            documentId = "doc-onboarding-android-setup",
        ),
        KnowledgeCheckpoint(
            id = "checkpoint-authentication",
            order = 3,
            title = "iOS Authentication",
            instruction = "Review the \"Authentication\" document to understand silent token refresh and session recovery. When you're done, tell me \"done\".",
            documentId = "doc-authentication",
        ),
        KnowledgeCheckpoint(
            id = "checkpoint-loyalty",
            order = 4,
            title = "Loyalty Program",
            instruction = "Review \"Loyalty Rewards Flow\" to understand point accumulation, redemption and tiers. When you're done, tell me \"done\".",
            documentId = "doc-loyalty",
        ),
        KnowledgeCheckpoint(
            id = "checkpoint-release-process",
            order = 5,
            title = "Release Process",
            instruction = "Review \"Release Process\" to understand how each release is validated and published. When you're done, tell me \"done\".",
            documentId = "doc-release-process",
        ),
    )

    private val quizBank: Map<String, List<QuizQuestion>> = mapOf(
        "checkpoint-welcome" to listOf(
            QuizQuestion(
                id = "q-welcome-1", checkpointId = "checkpoint-welcome",
                prompt = "Where does Shell's documentation live?",
                options = listOf(
                    "In Documents > Onboarding",
                    "In an external Google Doc",
                    "Documentation doesn't exist",
                    "Only in Confluence"
                ),
                correctOptionIndex = 0,
            ),
            QuizQuestion(
                id = "q-welcome-2", checkpointId = "checkpoint-welcome",
                prompt = "What can you ask the assistant?",
                options = listOf(
                    "Only create documents",
                    "Answer questions using indexed documentation",
                    "Nothing, it's just a chat",
                    "Delete documents"
                ),
                correctOptionIndex = 1,
            ),
        ),
        "checkpoint-android-setup" to listOf(
            QuizQuestion(
                id = "q-android-1", checkpointId = "checkpoint-android-setup",
                prompt = "What do you need to prepare per the Android Developer Setup guide?",
                options = listOf(
                    "Your machine and the cloned repo",
                    "Only an iOS emulator",
                    "Nothing, it's already set up",
                    "A Confluence account"
                ),
                correctOptionIndex = 0,
            ),
            QuizQuestion(
                id = "q-android-2", checkpointId = "checkpoint-android-setup",
                prompt = "Are the configuration values in the document...?",
                options = listOf("Real production values", "MOCK", "Random and meaningless", "QA-only"),
                correctOptionIndex = 1,
            ),
        ),
        "checkpoint-authentication" to listOf(
            QuizQuestion(
                id = "q-auth-1", checkpointId = "checkpoint-authentication",
                prompt = "What does silent token refresh solve?",
                options = listOf(
                    "Avoids asking the user to log in again constantly",
                    "Clears the session every hour",
                    "Increases security by deleting tokens",
                    "It's unrelated to sessions"
                ),
                correctOptionIndex = 0,
            ),
            QuizQuestion(
                id = "q-auth-2", checkpointId = "checkpoint-authentication",
                prompt = "What does session recovery cover?",
                options = listOf(
                    "What happens when the token expires or fails",
                    "Only the login screen's visual design",
                    "Nothing relevant to auth",
                    "User billing"
                ),
                correctOptionIndex = 0,
            ),
        ),
        "checkpoint-loyalty" to listOf(
            QuizQuestion(
                id = "q-loyalty-1", checkpointId = "checkpoint-loyalty",
                prompt = "What does \"Loyalty Rewards Flow\" describe?",
                options = listOf(
                    "Point accumulation, redemption and tiers",
                    "Just the program's logo",
                    "How releases are done",
                    "The backend architecture"
                ),
                correctOptionIndex = 0,
            ),
            QuizQuestion(
                id = "q-loyalty-2", checkpointId = "checkpoint-loyalty",
                prompt = "What do you need to understand about tiers?",
                options = listOf(
                    "How they affect benefits and redemption",
                    "They're decorative, no effect",
                    "They only apply to employees",
                    "Tiers don't exist"
                ),
                correctOptionIndex = 0,
            ),
        ),
        "checkpoint-release-process" to listOf(
            QuizQuestion(
                id = "q-release-1", checkpointId = "checkpoint-release-process",
                prompt = "What does the Release Process validate?",
                options = listOf(
                    "How each release is validated and published",
                    "Only the app's design",
                    "Customer billing",
                    "Nothing related to releases"
                ),
                correctOptionIndex = 0,
            ),
            QuizQuestion(
                id = "q-release-2", checkpointId = "checkpoint-release-process",
                prompt = "Why is it important to follow this process?",
                options = listOf(
                    "It ensures quality and consistent publishing",
                    "It's optional and doesn't matter",
                    "It only applies to iOS",
                    "It replaces QA"
                ),
                correctOptionIndex = 0,
            ),
        ),
    )

    private val completedIds = MutableStateFlow<Set<String>>(emptySet())
    private val attempts = MutableStateFlow<List<QuizAttempt>>(emptyList())

    override suspend fun checkpoints(): DomainResult<List<KnowledgeCheckpoint>> =
        DomainResult.success(items.sortedBy { it.order })

    override suspend fun progress(): DomainResult<KnowledgeProgress> =
        DomainResult.success(currentProgress())

    override suspend fun complete(checkpointId: String): DomainResult<KnowledgeProgress> {
        completedIds.value = completedIds.value + checkpointId
        return DomainResult.success(currentProgress())
    }

    override suspend fun quiz(checkpointId: String): DomainResult<List<QuizQuestion>> =
        DomainResult.success(quizBank[checkpointId].orEmpty())

    override suspend fun submitQuiz(checkpointId: String, answers: Map<String, Int>): DomainResult<QuizAttempt> {
        val questions = quizBank[checkpointId].orEmpty()
        val correct = questions.count { answers[it.id] == it.correctOptionIndex }
        val attempt = QuizAttempt(
            checkpointId = checkpointId,
            correct = correct,
            total = questions.size,
            submittedAt = timeProvider.now(),
        )
        attempts.value = attempts.value + attempt
        return DomainResult.success(attempt)
    }

    override suspend fun quizAttempts(): DomainResult<List<QuizAttempt>> =
        DomainResult.success(attempts.value)

    private fun currentProgress(): KnowledgeProgress =
        KnowledgeProgress(completed = completedIds.value.size, total = items.size)
}
