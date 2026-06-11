package com.shelldocs.core.domain.usecase.document

internal object KnowledgeQueryExpander {

    private val aliases: Map<String, List<String>> = mapOf(
        "release build" to listOf("eosb", "eosb1", "build generation", "versioncodes", "github actions", "qa handoff"),
        "pilot branch" to listOf("extra/pilot", "madf/pilot", "qa smoke test", "pilot"),
        "localization" to listOf("lokalise", "strings.xml", "translations", "l10n", "i18n"),
        "secrets" to listOf("azure secrets", "keychain", "environment values", "credentials"),
        "auth" to listOf("authentication", "login", "token", "oauth", "keychain"),
        "android release" to listOf("eosb", "build", "apk", "versioncode"),
        "deep link" to listOf("branch", "deeplinking", "uri", "intent"),
        "rewards" to listOf("loyalty", "points", "redemption", "shell go+"),
        "onboarding" to listOf(
            "kt", "knowledge transfer", "new collaborator", "getting started",
            "authentication", "release process", "nuevo colaborador", "incorporación",
            "nouveau collaborateur",
        ),
        "knowledge transfer" to listOf("onboarding", "authentication", "release process"),
        "kt session" to listOf("onboarding", "authentication", "release process"),
        "nuevo colaborador" to listOf("onboarding", "authentication", "release process"),
        "nouveau collaborateur" to listOf("onboarding", "authentication", "release process"),
    )

    private val stopWords = setOf(
        "the", "and", "for", "with", "what", "how", "does", "this", "that", "are",
        "los", "las", "del", "que", "como", "cómo", "para", "con", "una", "uno", "por",
    )
    private val nonWord = Regex("[^a-z0-9áéíóúñü+./-]+")

    fun expandedTerms(query: String): List<String> {
        val normalized = query.trim().lowercase()
        if (normalized.isBlank()) return emptyList()

        val phrases = buildList {
            add(normalized)
            aliases.forEach { (key, values) ->
                if (normalized.contains(key) || key.contains(normalized)) {
                    add(key)
                    addAll(values)
                }
            }
        }.distinct()

        return (phrases + phrases.flatMap(::tokenize))
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
    }

    fun tokenize(text: String): List<String> =
        text.lowercase()
            .split(nonWord)
            .filter { it.length >= MIN_TERM_LENGTH && it !in stopWords }
            .distinct()

    private const val MIN_TERM_LENGTH = 3
}
