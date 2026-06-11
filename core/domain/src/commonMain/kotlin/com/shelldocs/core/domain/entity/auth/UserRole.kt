package com.shelldocs.core.domain.entity.auth

/**
 * Roles delegated through the Supabase `user_roles` table
 * (see supabase/migrations/0001_identity_and_roles.sql).
 *
 * [key] matches the `roles.key` column exactly.
 */
enum class UserRole(val key: String, val displayName: String) {
    OWNER(key = "owner", displayName = "Owner"),
    BUSINESS(key = "business", displayName = "Business"),
    DEVELOP(key = "develop", displayName = "Develop"),
    VIEWER(key = "viewer", displayName = "Viewer");

    companion object {
        /** Unknown keys degrade to the least privileged role. */
        fun fromKey(key: String?): UserRole =
            entries.firstOrNull { it.key == key?.lowercase() } ?: VIEWER
    }
}
