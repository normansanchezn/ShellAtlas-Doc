package com.shelldocs.core.domain.usecase.auth

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.repository.AuthRepository

class SignOutUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(): DomainResult<Unit> = authRepository.signOut()
}
