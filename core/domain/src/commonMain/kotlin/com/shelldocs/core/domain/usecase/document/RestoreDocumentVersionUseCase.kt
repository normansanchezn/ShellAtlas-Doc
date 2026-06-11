package com.shelldocs.core.domain.usecase.document

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.Permission
import com.shelldocs.core.domain.entity.auth.RolePermissions
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.repository.DocumentRepository

class RestoreDocumentVersionUseCase(private val documentRepository: DocumentRepository) {

    suspend operator fun invoke(
        actorRole: UserRole,
        id: String,
        versionId: String,
    ): DomainResult<Document> {
        if (!RolePermissions.isGranted(actorRole, Permission.EDIT_DOCUMENTS)) {
            return DomainResult.failure(AppError.Unauthorized("Your role cannot restore versions"))
        }
        return documentRepository.restoreVersion(id, versionId)
    }
}
