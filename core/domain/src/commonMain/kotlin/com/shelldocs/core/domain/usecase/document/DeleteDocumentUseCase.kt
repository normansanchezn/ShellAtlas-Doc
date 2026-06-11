package com.shelldocs.core.domain.usecase.document

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.Permission
import com.shelldocs.core.domain.entity.auth.RolePermissions
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.repository.DocumentRepository

class DeleteDocumentUseCase(private val documentRepository: DocumentRepository) {

    suspend operator fun invoke(actorRole: UserRole, id: String): DomainResult<Unit> {
        if (!RolePermissions.isGranted(actorRole, Permission.DELETE_DOCUMENTS)) {
            return DomainResult.failure(AppError.Unauthorized("Only owners can delete documents"))
        }
        return documentRepository.delete(id)
    }
}
