package com.shelldocs.core.data.supabase

import com.shelldocs.core.common.logging.AppLogger
import com.shelldocs.core.common.logging.LogTags
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * Minimal PostgREST client: typed reads and writes with row-level security
 * enforced by the user's access token.
 *
 * Every operation is logged under [LogTags.CRUD] (mirrors `LogTags.DATABASE`
 * for connectivity) so any repository built on top of this client gets
 * CRUD tracing for free — no need to instrument each repository separately.
 */
class SupabasePostgrestApi(
    @PublishedApi internal val httpClient: HttpClient,
    @PublishedApi internal val config: SupabaseConfig,
    @PublishedApi internal val accessTokenProvider: () -> String?,
) {
    @PublishedApi
    internal val logger = AppLogger.tag(LogTags.CRUD)

    suspend inline fun <reified T> select(
        table: String,
        query: String,
        accessTokenOverride: String? = null,
    ): T {
        logger.d("SELECT $table ($query)")
        val response = httpClient.get(urlFor(table, query)) {
            headers {
                append("apikey", config.anonKey)
                (accessTokenOverride ?: accessTokenProvider())
                    ?.let { append("Authorization", "Bearer $it") }
            }
        }
        if (!response.status.isSuccess()) {
            logger.e("SELECT $table failed with ${response.status.value}")
            throw SupabasePostgrestException("SELECT $table failed with ${response.status.value}")
        }
        return response.body()
    }

    suspend inline fun <reified Response, reified Body> insert(
        table: String,
        body: Body,
        accessTokenOverride: String? = null,
    ): Response {
        logger.i("INSERT $table")
        val response: HttpResponse = httpClient.post("${config.restBaseUrl}/$table") {
            headers {
                append("apikey", config.anonKey)
                (accessTokenOverride ?: accessTokenProvider())
                    ?.let { append("Authorization", "Bearer $it") }
                append("Prefer", "return=representation")
            }
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        if (!response.status.isSuccess()) {
            logger.e("INSERT $table failed with ${response.status.value}")
            throw SupabasePostgrestException("INSERT $table failed with ${response.status.value}")
        }
        logger.i("INSERT $table succeeded")
        return response.body()
    }

    suspend inline fun <reified Body> upsert(
        table: String,
        body: Body,
        accessTokenOverride: String? = null,
    ) {
        logger.i("UPSERT $table")
        val response: HttpResponse = httpClient.post("${config.restBaseUrl}/$table") {
            headers {
                append("apikey", config.anonKey)
                (accessTokenOverride ?: accessTokenProvider())
                    ?.let { append("Authorization", "Bearer $it") }
                append("Prefer", "resolution=merge-duplicates,return=minimal")
            }
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        if (!response.status.isSuccess()) {
            logger.e("UPSERT $table failed with ${response.status.value}")
            throw SupabasePostgrestException("UPSERT $table failed with ${response.status.value}")
        }
        logger.i("UPSERT $table succeeded")
    }

    suspend inline fun <reified Response, reified Body> update(
        table: String,
        query: String,
        body: Body,
        accessTokenOverride: String? = null,
    ): Response {
        logger.i("UPDATE $table ($query)")
        val response: HttpResponse = httpClient.patch(urlFor(table, query)) {
            headers {
                append("apikey", config.anonKey)
                (accessTokenOverride ?: accessTokenProvider())
                    ?.let { append("Authorization", "Bearer $it") }
                append("Prefer", "return=representation")
            }
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        if (!response.status.isSuccess()) {
            logger.e("UPDATE $table failed with ${response.status.value}")
            throw SupabasePostgrestException("UPDATE $table failed with ${response.status.value}")
        }
        logger.i("UPDATE $table succeeded")
        return response.body()
    }

    suspend fun delete(
        table: String,
        query: String,
        accessTokenOverride: String? = null,
    ) {
        logger.i("DELETE $table ($query)")
        val response = httpClient.delete(urlFor(table, query)) {
            headers {
                append("apikey", config.anonKey)
                (accessTokenOverride ?: accessTokenProvider())
                    ?.let { append("Authorization", "Bearer $it") }
            }
        }
        if (!response.status.isSuccess()) {
            logger.e("DELETE $table failed with ${response.status.value}")
            throw SupabasePostgrestException("DELETE $table failed with ${response.status.value}")
        }
        logger.i("DELETE $table succeeded")
    }

    /** Lightweight reachability + auth check, used at startup to confirm the DB is reachable. */
    suspend fun testConnection(): Result<Unit> {
        val dbLogger = AppLogger.tag(LogTags.DATABASE)
        return runCatching {
            dbLogger.i("Checking Supabase connection at ${config.restBaseUrl}")
            val response = httpClient.get(config.restBaseUrl) {
                headers { append("apikey", config.anonKey) }
            }
            if (!response.status.isSuccess()) {
                throw SupabasePostgrestException("Connection check failed with ${response.status.value}")
            }
            dbLogger.i("Supabase connection OK (${response.status.value})")
        }.onFailure { error ->
            dbLogger.e("Supabase connection FAILED: ${error.message}", error)
        }
    }

    @PublishedApi
    internal fun urlFor(table: String, query: String): String =
        if (query.isBlank()) "${config.restBaseUrl}/$table" else "${config.restBaseUrl}/$table?$query"
}
