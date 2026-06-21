package com.shelldocs.core.domain.entity.auth

/** Authenticated member of the workspace. */
data class UserProfile(
    val id: String,
    val email: String,
    val fullName: String,
    val team: String,
    val role: UserRole,
    val language: AppLanguage = AppLanguage.ENGLISH,
) {
    val initials: String =
        fullName.split(' ')
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifEmpty { email.take(2).uppercase() }
}
