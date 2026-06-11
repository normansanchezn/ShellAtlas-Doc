package com.shelldocs.core.domain.entity.auth

/** Raw credentials captured by the sign-in form; validated by SignInUseCase. */
data class SignInCredentials(
    val email: String,
    val password: String,
)
