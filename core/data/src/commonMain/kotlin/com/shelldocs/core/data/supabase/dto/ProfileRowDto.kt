package com.shelldocs.core.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Row of `public.profiles`. */
@Serializable
data class ProfileRowDto(
    val id: String,
    @SerialName("full_name") val fullName: String = "",
    val team: String = "",
    val email: String = "",
    val language: String = "en",
)
