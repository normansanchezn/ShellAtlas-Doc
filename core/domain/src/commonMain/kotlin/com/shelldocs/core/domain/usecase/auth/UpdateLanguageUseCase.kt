package com.shelldocs.core.domain.usecase.auth

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.AppLanguage
import com.shelldocs.core.domain.entity.auth.UserProfile
import com.shelldocs.core.domain.repository.AuthRepository

class UpdateLanguageUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(language: AppLanguage): DomainResult<UserProfile> =
        authRepository.updateLanguage(language)
}
