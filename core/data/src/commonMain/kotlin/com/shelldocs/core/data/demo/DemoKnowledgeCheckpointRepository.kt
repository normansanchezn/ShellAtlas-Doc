package com.shelldocs.core.data.demo

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.onboarding.KnowledgeCheckpoint
import com.shelldocs.core.domain.entity.onboarding.KnowledgeProgress
import com.shelldocs.core.domain.repository.KnowledgeCheckpointRepository
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory guided knowledge-transfer checklist for the current session. */
class DemoKnowledgeCheckpointRepository : KnowledgeCheckpointRepository {

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

    private val completedIds = MutableStateFlow<Set<String>>(emptySet())

    override suspend fun checkpoints(): DomainResult<List<KnowledgeCheckpoint>> =
        DomainResult.success(items.sortedBy { it.order })

    override suspend fun progress(): DomainResult<KnowledgeProgress> =
        DomainResult.success(currentProgress())

    override suspend fun complete(checkpointId: String): DomainResult<KnowledgeProgress> {
        completedIds.value = completedIds.value + checkpointId
        return DomainResult.success(currentProgress())
    }

    private fun currentProgress(): KnowledgeProgress =
        KnowledgeProgress(completed = completedIds.value.size, total = items.size)
}
