package com.shelldocs.core.data.supabase

import com.shelldocs.core.data.supabase.dto.ProfileRowDto

/** Reads `public.profiles` rows through PostgREST. */
class SupabaseProfileDataSource(private val postgrest: SupabasePostgrestApi) {

    suspend fun profile(userId: String): ProfileRowDto? =
        postgrest.select<List<ProfileRowDto>>(
            table = "profiles",
            query = "id=eq.$userId&select=id,full_name,team,email",
        ).firstOrNull()

    suspend fun allProfiles(): List<ProfileRowDto> =
        postgrest.select(
            table = "profiles",
            query = "select=id,full_name,team,email&order=full_name.asc",
        )
}
