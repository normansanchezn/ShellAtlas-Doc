package com.shelldocs.core.data.supabase.dto

import kotlinx.serialization.Serializable

/** Body of the GoTrue `token?grant_type=password` request. */
@Serializable
data class PasswordGrantRequestDto(
    val email: String,
    val password: String,
)
