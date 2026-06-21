package com.shelldocs.core.data.assistant

import com.shelldocs.core.common.logging.AppLogger
import com.shelldocs.core.common.logging.LogTags
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

/** Minimal Ollama HTTP client (`/api/generate`, `/api/tags`). */
class OllamaClient(
    private val httpClient: HttpClient,
    private val config: OllamaConfig,
) {
    private val logger = AppLogger.tag(LogTags.OLLAMA)

    suspend fun generate(prompt: String): String {
        logger.i("generate() -> ${config.baseUrl} model=${config.model}")
        val response = try {
            httpClient.post("${config.baseUrl.trimEnd('/')}/api/generate") {
                contentType(ContentType.Application.Json)
                setBody(
                    OllamaGenerateRequestDto(
                        model = config.model,
                        prompt = prompt,
                        stream = false,
                        options = OllamaOptionsDto(numCtx = config.contextWindow),
                    ),
                )
            }
        } catch (error: Exception) {
            logger.e("generate() failed to reach Ollama at ${config.baseUrl}: ${error.message}", error)
            throw error
        }
        if (!response.status.isSuccess()) {
            logger.e("generate() returned ${response.status.value}")
            check(response.status.isSuccess()) { "Ollama returned ${response.status.value}" }
        }
        val rawBody = response.bodyAsText()
        val parsed = try {
            RESPONSE_JSON.decodeFromString<OllamaGenerateResponseDto>(rawBody)
        } catch (error: Exception) {
            logger.e(
                "generate() got HTTP ${response.status.value} but failed to parse the body " +
                        "(${error.message}). Raw body (first 500 chars): ${rawBody.take(500)}",
                error,
            )
            throw error
        }
        logger.i("generate() succeeded")
        return parsed.response
    }

    /** Checks `/api/tags`; also used as a startup connectivity probe. */
    suspend fun isReachable(): Boolean {
        logger.i("Checking Ollama connection at ${config.baseUrl}")
        return runCatching {
            httpClient.get("${config.baseUrl.trimEnd('/')}/api/tags").status.isSuccess()
        }.onSuccess { reachable ->
            if (reachable) {
                logger.i("Ollama connection OK (model=${config.model})")
            } else {
                logger.w("Ollama responded but reported failure")
            }
        }.onFailure { error ->
            logger.e("Ollama connection FAILED: ${error.message}", error)
        }.getOrDefault(false)
    }

    val modelName: String get() = config.model

    private companion object {
        val RESPONSE_JSON = Json { ignoreUnknownKeys = true; isLenient = true }
    }
}
