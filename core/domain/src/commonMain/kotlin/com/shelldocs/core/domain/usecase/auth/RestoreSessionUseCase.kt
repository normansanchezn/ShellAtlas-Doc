package com.shelldocs.core.domain.usecase.auth

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.AuthSession
import com.shelldocs.core.domain.repository.AuthRepository

class RestoreSessionUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(): DomainResult<AuthSession?> = authRepository.restoreSession()
}
