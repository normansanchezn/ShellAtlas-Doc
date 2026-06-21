package com.shelldocs.core.data.supabase

import com.shelldocs.core.data.supabase.dto.ProfileRowDto

/** Reads `public.profiles` rows through PostgREST. */
class SupabaseProfileDataSource(private val postgrest: SupabasePostgrestApi) {

    suspend fun profile(userId: String, accessTokenOverride: String? = null): ProfileRowDto? =
        postgrest.select<List<ProfileRowDto>>(
            table = "profiles",
            query = "id=eq.$userId&select=id,full_name,team,email,language",
            accessTokenOverride = accessTokenOverride,
        ).firstOrNull()

    suspend fun allProfiles(accessTokenOverride: String? = null): List<ProfileRowDto> =
        postgrest.select(
            table = "profiles",
            query = "select=id,full_name,team,email,language&order=full_name.asc",
            accessTokenOverride = accessTokenOverride,
        )

    suspend fun updateLanguage(
        userId: String,
        language: String,
        accessTokenOverride: String? = null,
    ): ProfileRowDto =
        postgrest.update<List<ProfileRowDto>, Map<String, String>>(
            table = "profiles",
            query = "id=eq.$userId",
            body = mapOf("language" to language),
            accessTokenOverride = accessTokenOverride,
        ).first()
}
