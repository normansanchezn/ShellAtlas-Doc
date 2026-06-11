package com.shelldocs.core.data.assistant

/** Local LLM (Ollama) connection settings, mirroring the original env vars. */
data class OllamaConfig(
    val baseUrl: String = "http://127.0.0.1:11434",
    val model: String = "llama3.2",
)
