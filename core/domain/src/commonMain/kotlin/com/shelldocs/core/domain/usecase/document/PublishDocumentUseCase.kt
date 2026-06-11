package com.shelldocs.core.domain.usecase.document

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.Permission
import com.shelldocs.core.domain.entity.auth.RolePermissions
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.repository.DocumentRepository

class PublishDocumentUseCase(private val documentRepository: DocumentRepository) {

    suspend operator fun invoke(
        actorRole: UserRole,
        id: String,
        markdown: String,
        changeSummary: String,
    ): DomainResult<Document> {
        if (!RolePermissions.isGranted(actorRole, Permission.PUBLISH_DOCUMENTS)) {
            return DomainResult.failure(AppError.Unauthorized("Your role cannot publish documents"))
        }
        if (markdown.isBlank()) {
            return DomainResult.failure(AppError.Validation("Cannot publish an empty document"))
        }
        return documentRepository.publish(id, markdown, changeSummary.ifBlank { "Updated content" })
    }
}
