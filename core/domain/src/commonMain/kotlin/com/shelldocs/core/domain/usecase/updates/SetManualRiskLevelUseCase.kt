package com.shelldocs.core.domain.usecase.updates

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.entity.updates.PendingUpdate
import com.shelldocs.core.domain.entity.updates.RiskLevel
import com.shelldocs.core.domain.repository.PendingUpdatesRepository

/** Only administrators may assign or clear the manual Medium Risk classification. */
class SetManualRiskLevelUseCase(private val pendingUpdatesRepository: PendingUpdatesRepository) {

    suspend operator fun invoke(actorRole: UserRole, documentId: String, risk: RiskLevel?): DomainResult<PendingUpdate> {
        if (actorRole != UserRole.OWNER) {
            return DomainResult.failure(AppError.Unauthorized("Only administrators can set the risk classification"))
        }
        if (risk != null && risk != RiskLevel.MEDIUM) {
            return DomainResult.failure(AppError.Validation("Manual override must be Medium Risk, or cleared"))
        }
        return pendingUpdatesRepository.setManualRisk(documentId, risk)
    }
}
