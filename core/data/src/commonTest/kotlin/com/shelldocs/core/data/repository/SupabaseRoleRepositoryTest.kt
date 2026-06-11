package com.shelldocs.core.data.repository

import com.shelldocs.core.common.result.getOrDefault
import com.shelldocs.core.common.result.getOrNull
import com.shelldocs.core.data.supabase.SupabaseConfig
import com.shelldocs.core.data.supabase.SupabasePostgrestApi
import com.shelldocs.core.data.supabase.SupabaseProfileDataSource
import com.shelldocs.core.domain.entity.auth.UserRole
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SupabaseRoleRepositoryTest {

    private val config = SupabaseConfig(url = "https://demo.supabase.co", anonKey = "anon-key")

    private fun repository(): SupabaseRoleRepository {
        val engine = MockEngine { request ->
            assertEquals("Bearer at-123", request.headers[HttpHeaders.Authorization])
            val body = when {
                "user_roles" in request.url.encodedPath && "user_id=eq.user-1" in request.url.encodedQuery ->
                    """[{"user_id":"user-1","role_key":"owner"}]"""
                "user_roles" in request.url.encodedPath ->
                    """[{"user_id":"user-1","role_key":"owner"},{"user_id":"user-2","role_key":"develop"}]"""
                "profiles" in request.url.encodedPath ->
                    """[
                        {"id":"user-1","full_name":"Elena Vargas","team":"iOS Shell App","email":"elena@shell.com"},
                        {"id":"user-2","full_name":"Marcus Chen","team":"Loyalty Squad","email":"marcus@shell.com"},
                        {"id":"user-3","full_name":"Sin Rol","team":"QA","email":"sinrol@shell.com"}
                    ]"""
                else -> "[]"
            }
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        val postgrest = SupabasePostgrestApi(client, config) { "at-123" }
        return SupabaseRoleRepository(
            postgrest = postgrest,
            profiles = SupabaseProfileDataSource(postgrest),
            currentUserIdProvider = { "user-1" },
        )
    }

    @Test
    fun roleOfReadsTheUserRolesTable() = runTest {
        assertEquals(UserRole.OWNER, repository().roleOf("user-1").getOrNull())
    }

    @Test
    fun teamMembersJoinProfilesWithRolesAndDefaultToViewer() = runTest {
        val members = repository().teamMembers().getOrDefault(emptyList())

        assertEquals(3, members.size)
        assertEquals(UserRole.OWNER, members.first { it.profile.id == "user-1" }.profile.role)
        assertEquals(UserRole.DEVELOP, members.first { it.profile.id == "user-2" }.profile.role)
        assertEquals(UserRole.VIEWER, members.first { it.profile.id == "user-3" }.profile.role)
        assertTrue(members.first { it.profile.id == "user-1" }.isCurrentUser)
    }
}
