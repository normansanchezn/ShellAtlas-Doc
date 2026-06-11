package com.shelldocs.core.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Row of `public.user_roles`; role delegation lives in this table. */
@Serializable
data class UserRoleRowDto(
    @SerialName("user_id") val userId: String,
    @SerialName("role_key") val roleKey: String,
)
