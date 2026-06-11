package com.shelldocs.core.domain.usecase.auth

import com.shelldocs.core.domain.entity.auth.AuthSession
import com.shelldocs.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveSessionUseCase(private val authRepository: AuthRepository) {

    operator fun invoke(): StateFlow<AuthSession?> = authRepository.session
}
