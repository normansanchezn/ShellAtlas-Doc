package com.shelldocs.core.data.supabase

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
import kotlin.test.assertFailsWith

class SupabaseAuthApiTest {

    private val config = SupabaseConfig(url = "https://demo.supabase.co", anonKey = "anon-key")

    private fun apiRespondingWith(status: HttpStatusCode, body: String): SupabaseAuthApi {
        val engine = MockEngine { request ->
            assertEquals("anon-key", request.headers["apikey"])
            respond(
                content = body,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        return SupabaseAuthApi(client, config)
    }

    @Test
    fun successfulSignInParsesTokensAndUser() = runTest {
        val api = apiRespondingWith(
            HttpStatusCode.OK,
            """
            {
              "access_token": "at-123",
              "refresh_token": "rt-456",
              "expires_in": 3600,
              "user": { "id": "user-1", "email": "elena.vargas@shell.com" }
            }
            """.trimIndent(),
        )

        val response = api.signInWithPassword("elena.vargas@shell.com", "secret-123")

        assertEquals("at-123", response.accessToken)
        assertEquals("rt-456", response.refreshToken)
        assertEquals("user-1", response.user.id)
    }

    @Test
    fun badCredentialsRaiseAuthException() = runTest {
        val api = apiRespondingWith(HttpStatusCode.BadRequest, """{"error":"invalid_grant"}""")

        assertFailsWith<SupabaseAuthException> {
            api.signInWithPassword("elena.vargas@shell.com", "wrong")
        }
    }

    @Test
    fun serverErrorsRaiseAuthException() = runTest {
        val api = apiRespondingWith(HttpStatusCode.InternalServerError, "{}")

        assertFailsWith<SupabaseAuthException> {
            api.signInWithPassword("elena.vargas@shell.com", "secret-123")
        }
    }
}
