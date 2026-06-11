package com.shelldocs.core.data.supabase

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/**
 * Minimal PostgREST client: typed reads and upserts with row-level security
 * enforced by the user's access token.
 */
class SupabasePostgrestApi(
    @PublishedApi internal val httpClient: HttpClient,
    @PublishedApi internal val config: SupabaseConfig,
    @PublishedApi internal val accessTokenProvider: () -> String?,
) {

    suspend inline fun <reified T> select(table: String, query: String): T {
        val response = httpClient.get("${config.restBaseUrl}/$table?$query") {
            headers {
                append("apikey", config.anonKey)
                accessTokenProvider()?.let { append("Authorization", "Bearer $it") }
            }
        }
        if (!response.status.isSuccess()) {
            throw SupabasePostgrestException("SELECT $table failed with ${response.status.value}")
        }
        return response.body()
    }

    suspend inline fun <reified Body> upsert(table: String, body: Body) {
        val response: HttpResponse = httpClient.post("${config.restBaseUrl}/$table") {
            headers {
                append("apikey", config.anonKey)
                accessTokenProvider()?.let { append("Authorization", "Bearer $it") }
                append("Prefer", "resolution=merge-duplicates,return=minimal")
            }
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        if (!response.status.isSuccess()) {
            throw SupabasePostgrestException("UPSERT $table failed with ${response.status.value}")
        }
    }
}
