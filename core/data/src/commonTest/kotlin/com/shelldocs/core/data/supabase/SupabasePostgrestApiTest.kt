package com.shelldocs.core.data.supabase

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class SupabasePostgrestApiTest {

    private val config = SupabaseConfig(url = "https://demo.supabase.co", anonKey = "anon-key")

    private fun api(handler: (io.ktor.client.request.HttpRequestData) -> Unit): SupabasePostgrestApi {
        val engine = MockEngine { request ->
            handler(request)
            respond("[]", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        return SupabasePostgrestApi(client, config) { "at-123" }
    }

    @Test
    fun selectUsesRestBaseUrlAndBearerToken() = runTest {
        val api = api { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("/rest/v1/documents", request.url.encodedPath)
            assertEquals("select=*", request.url.encodedQuery)
            assertEquals("anon-key", request.headers["apikey"])
            assertEquals("Bearer at-123", request.headers[HttpHeaders.Authorization])
        }

        api.select<List<Map<String, String>>>("documents", "select=*")
    }

    @Test
    fun insertAndUpsertAndUpdateReuseTheSameAuthHeaders() = runTest {
        val calls = mutableListOf<String>()
        val api = api { request ->
            calls += "${request.method.value} ${request.url.encodedPath}?${request.url.encodedQuery}"
            assertEquals("anon-key", request.headers["apikey"])
            assertEquals("Bearer at-123", request.headers[HttpHeaders.Authorization])
        }

        api.insert<List<Map<String, String>>, Map<String, String>>("documents", mapOf("title" to "Doc"))
        api.upsert("document_drafts", mapOf("document_id" to "doc-1"))
        api.update<List<Map<String, String>>, Map<String, String>>("documents", "id=eq.doc-1", mapOf("title" to "Doc"))

        assertEquals(3, calls.size)
        assertEquals("POST /rest/v1/documents?", calls[0])
        assertEquals("POST /rest/v1/document_drafts?", calls[1])
        assertEquals("PATCH /rest/v1/documents?id=eq.doc-1", calls[2])
    }
}
