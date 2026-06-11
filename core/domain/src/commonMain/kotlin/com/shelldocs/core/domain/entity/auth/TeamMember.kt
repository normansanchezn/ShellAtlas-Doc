package com.shelldocs.core.domain.entity.auth

/** Workspace member as listed in Settings -> Team & Access. */
data class TeamMember(
    val profile: UserProfile,
    val isCurrentUser: Boolean = false,
)
