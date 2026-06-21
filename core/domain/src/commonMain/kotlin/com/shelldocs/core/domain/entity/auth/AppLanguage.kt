package com.shelldocs.core.domain.entity.auth

/** UI display language, persisted per-user on `profiles.language`. */
enum class AppLanguage(val code: String) {
    ENGLISH("en"),
    SPANISH("es"),
    FRENCH("fr"),
    ;

    companion object {
        fun fromCode(code: String?): AppLanguage = entries.firstOrNull { it.code == code } ?: ENGLISH
    }
}
