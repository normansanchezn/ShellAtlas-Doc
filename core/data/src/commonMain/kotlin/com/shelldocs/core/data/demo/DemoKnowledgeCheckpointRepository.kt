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
            title = "Llega a Shell",
            instruction = "Revisa el documento \"Llega a Shell\" en Documentos > Onboarding para conocer el equipo, dónde vive la documentación y cómo funciona este asistente. Cuando termines, dime \"listo\".",
            documentId = "doc-onboarding-welcome",
        ),
        KnowledgeCheckpoint(
            id = "checkpoint-android-setup",
            order = 2,
            title = "Configura tu entorno Android",
            instruction = "Revisa \"Android Developer Setup\" en Documentos > Onboarding para clonar el repositorio y preparar tu máquina (valores son MOCK). Cuando termines, dime \"listo\".",
            documentId = "doc-onboarding-android-setup",
        ),
        KnowledgeCheckpoint(
            id = "checkpoint-authentication",
            order = 3,
            title = "Autenticación iOS",
            instruction = "Revisa el documento \"Authentication\" para entender el refresco silencioso de tokens y la recuperación de sesión. Cuando termines, dime \"listo\".",
            documentId = "doc-authentication",
        ),
        KnowledgeCheckpoint(
            id = "checkpoint-loyalty",
            order = 4,
            title = "Programa de Lealtad",
            instruction = "Revisa \"Loyalty Rewards Flow\" para entender la acumulación de puntos, redención y niveles. Cuando termines, dime \"listo\".",
            documentId = "doc-loyalty",
        ),
        KnowledgeCheckpoint(
            id = "checkpoint-release-process",
            order = 5,
            title = "Proceso de Release",
            instruction = "Revisa \"Release Process\" para entender cómo se valida y publica cada release. Cuando termines, dime \"listo\".",
            documentId = "doc-release-process",
        ),
    )

    private val quizBank: Map<String, List<QuizQuestion>> = mapOf(
        "checkpoint-welcome" to listOf(
            QuizQuestion(
                id = "q-welcome-1", checkpointId = "checkpoint-welcome",
                prompt = "¿Dónde vive la documentación de Shell?",
                options = listOf(
                    "En Documentos > Onboarding",
                    "En un Google Doc externo",
                    "No existe documentación",
                    "Solo en Confluence"
                ),
                correctOptionIndex = 0,
            ),
            QuizQuestion(
                id = "q-welcome-2", checkpointId = "checkpoint-welcome",
                prompt = "¿Qué puedes pedirle al asistente?",
                options = listOf(
                    "Solo crear documentos",
                    "Responder preguntas usando la documentación indexada",
                    "Nada, es solo un chat",
                    "Eliminar documentos"
                ),
                correctOptionIndex = 1,
            ),
        ),
        "checkpoint-android-setup" to listOf(
            QuizQuestion(
                id = "q-android-1", checkpointId = "checkpoint-android-setup",
                prompt = "¿Qué necesitas preparar según la guía de Android Developer Setup?",
                options = listOf(
                    "Tu máquina y el repo clonado",
                    "Solo un emulador iOS",
                    "Nada, ya está todo listo",
                    "Una cuenta de Confluence"
                ),
                correctOptionIndex = 0,
            ),
            QuizQuestion(
                id = "q-android-2", checkpointId = "checkpoint-android-setup",
                prompt = "¿Los valores de configuración del documento son...?",
                options = listOf("Reales y de producción", "MOCK", "Aleatorios sin sentido", "Solo para QA"),
                correctOptionIndex = 1,
            ),
        ),
        "checkpoint-authentication" to listOf(
            QuizQuestion(
                id = "q-auth-1", checkpointId = "checkpoint-authentication",
                prompt = "¿Qué resuelve el refresco silencioso de tokens?",
                options = listOf(
                    "Evita pedir login de nuevo constantemente",
                    "Borra la sesión cada hora",
                    "Aumenta la seguridad eliminando tokens",
                    "No tiene relación con sesión"
                ),
                correctOptionIndex = 0,
            ),
            QuizQuestion(
                id = "q-auth-2", checkpointId = "checkpoint-authentication",
                prompt = "¿Qué cubre la recuperación de sesión?",
                options = listOf(
                    "Qué pasa cuando el token expira o falla",
                    "Solo el diseño visual del login",
                    "Nada relevante para auth",
                    "La facturación del usuario"
                ),
                correctOptionIndex = 0,
            ),
        ),
        "checkpoint-loyalty" to listOf(
            QuizQuestion(
                id = "q-loyalty-1", checkpointId = "checkpoint-loyalty",
                prompt = "¿Qué describe \"Loyalty Rewards Flow\"?",
                options = listOf(
                    "Acumulación de puntos, redención y niveles",
                    "Solo el logo del programa",
                    "Cómo se hacen los releases",
                    "La arquitectura del backend"
                ),
                correctOptionIndex = 0,
            ),
            QuizQuestion(
                id = "q-loyalty-2", checkpointId = "checkpoint-loyalty",
                prompt = "¿Qué necesitas entender de los niveles?",
                options = listOf(
                    "Cómo afectan beneficios y redención",
                    "Son decorativos, sin efecto",
                    "Solo aplican a empleados",
                    "No existen niveles"
                ),
                correctOptionIndex = 0,
            ),
        ),
        "checkpoint-release-process" to listOf(
            QuizQuestion(
                id = "q-release-1", checkpointId = "checkpoint-release-process",
                prompt = "¿Qué valida el Proceso de Release?",
                options = listOf(
                    "Cómo se valida y publica cada release",
                    "Solo el diseño de la app",
                    "La facturación de clientes",
                    "Nada relacionado a releases"
                ),
                correctOptionIndex = 0,
            ),
            QuizQuestion(
                id = "q-release-2", checkpointId = "checkpoint-release-process",
                prompt = "¿Por qué es importante seguir este proceso?",
                options = listOf(
                    "Asegura calidad y publicación consistente",
                    "Es opcional y no importa",
                    "Solo aplica a iOS",
                    "Reemplaza al QA"
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
