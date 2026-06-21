package com.shelldocs.core.data.assistant

import com.shelldocs.core.common.logging.AppLogger
import com.shelldocs.core.common.logging.LogTags
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

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
                setBody(OllamaGenerateRequestDto(model = config.model, prompt = prompt))
            }
        } catch (error: Exception) {
            logger.e("generate() failed to reach Ollama at ${config.baseUrl}: ${error.message}", error)
            throw error
        }
        if (!response.status.isSuccess()) {
            logger.e("generate() returned ${response.status.value}")
            check(response.status.isSuccess()) { "Ollama returned ${response.status.value}" }
        }
        logger.i("generate() succeeded")
        return response.body<OllamaGenerateResponseDto>().response
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
}
