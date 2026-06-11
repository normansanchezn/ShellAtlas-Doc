package com.shelldocs.core.data.supabase

import com.shelldocs.core.data.supabase.dto.AuthTokenResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

/**
 * Thin GoTrue client. Only the endpoints ShellDocs needs — kept explicit so
 * every request is visible and mockable with Ktor's MockEngine.
 */
class SupabaseAuthApi(
    private val httpClient: HttpClient,
    private val config: SupabaseConfig,
) {

    @Serializable
    private data class PasswordGrantRequest(val email: String, val password: String)

    suspend fun signInWithPassword(email: String, password: String): AuthTokenResponseDto {
        val response = httpClient.post("${config.authBaseUrl}/token?grant_type=password") {
            headers { append("apikey", config.anonKey) }
            contentType(ContentType.Application.Json)
            setBody(PasswordGrantRequest(email, password))
        }
        if (response.status == HttpStatusCode.BadRequest || response.status == HttpStatusCode.Unauthorized) {
            throw SupabaseAuthException("Invalid credentials")
        }
        if (!response.status.isSuccess()) {
            throw SupabaseAuthException("Auth request failed with ${response.status.value}")
        }
        return response.body()
    }

    suspend fun signOut(accessToken: String) {
        httpClient.post("${config.authBaseUrl}/logout") {
            headers {
                append("apikey", config.anonKey)
                append("Authorization", "Bearer $accessToken")
            }
        }
    }
}
