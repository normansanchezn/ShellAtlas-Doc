package com.shelldocs.core.domain.usecase.auth

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.AuthSession
import com.shelldocs.core.domain.entity.auth.SignInCredentials
import com.shelldocs.core.domain.repository.AuthRepository

/**
 * Validates credentials before delegating to the identity provider, so the
 * UI and the data layer never duplicate validation rules.
 */
class SignInUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(credentials: SignInCredentials): DomainResult<AuthSession> {
        val email = credentials.email.trim()
        if (!EMAIL_REGEX.matches(email)) {
            return DomainResult.failure(AppError.Validation("Enter a valid corporate email"))
        }
        if (credentials.password.length < MIN_PASSWORD_LENGTH) {
            return DomainResult.failure(
                AppError.Validation("Password must have at least $MIN_PASSWORD_LENGTH characters"),
            )
        }
        return authRepository.signIn(credentials.copy(email = email))
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 8
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
