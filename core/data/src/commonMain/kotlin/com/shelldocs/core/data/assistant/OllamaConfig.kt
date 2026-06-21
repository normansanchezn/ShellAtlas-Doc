package com.shelldocs.core.data.assistant

/** Local LLM (Ollama) connection settings, mirroring the original env vars. */
data class OllamaConfig(
    val baseUrl: String = "http://127.0.0.1:11434",
    val model: String = "llama3.2",
    /** Context window in tokens. Ollama's own default (2048-4096 depending on model) is too
     *  small for prompts grounded on multiple full documents; raise it so long questions/answers
     *  don't get silently truncated. */
    val contextWindow: Int = 8192,
)
