package com.shelldocs.core.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class AuthUserDto(
    val id: String,
    val email: String = "",
    @SerialName("user_metadata") val userMetadata: JsonObject? = null,
)
