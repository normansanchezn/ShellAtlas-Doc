package com.shelldocs.core.domain.usecase.source

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.Permission
import com.shelldocs.core.domain.entity.auth.RolePermissions
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.entity.source.KnowledgeSource
import com.shelldocs.core.domain.repository.SourcesRepository

class SyncSourceUseCase(private val sourcesRepository: SourcesRepository) {

    suspend operator fun invoke(actorRole: UserRole, sourceId: String): DomainResult<KnowledgeSource> {
        if (!RolePermissions.isGranted(actorRole, Permission.RUN_SOURCE_SYNC)) {
            return DomainResult.failure(AppError.Unauthorized("Your role cannot trigger source syncs"))
        }
        return sourcesRepository.sync(sourceId)
    }
}
