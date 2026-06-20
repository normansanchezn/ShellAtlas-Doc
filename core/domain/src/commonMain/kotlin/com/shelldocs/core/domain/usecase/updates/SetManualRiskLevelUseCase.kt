package com.shelldocs.core.domain.usecase.updates

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.UserRole
import com.shelldocs.core.domain.entity.updates.PendingUpdate
import com.shelldocs.core.domain.entity.updates.RiskLevel
import com.shelldocs.core.domain.repository.PendingUpdatesRepository

/** Only administrators may assign or clear a manual risk classification, from the table's risk dropdown. */
class SetManualRiskLevelUseCase(private val pendingUpdatesRepository: PendingUpdatesRepository) {

    suspend operator fun invoke(actorRole: UserRole, documentId: String, risk: RiskLevel?): DomainResult<PendingUpdate> {
        if (actorRole != UserRole.OWNER) {
            return DomainResult.failure(AppError.Unauthorized("Only administrators can set the risk classification"))
        }
        if (risk != null && risk !in RiskLevel.DOCUMENTATION_HEALTH_LEVELS) {
            return DomainResult.failure(AppError.Validation("Risk must be Critical, Medium, or Low"))
        }
        return pendingUpdatesRepository.setManualRisk(documentId, risk)
    }
}
