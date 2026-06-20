package com.shelldocs.core.domain.entity.document

/**
 * Versions known to exist within ShellAtlas. Application Version fields must
 * always pick from here — never free text — and never a version older than
 * whatever is already assigned to the document.
 */
object ApplicationVersionCatalog {

    val KNOWN_VERSIONS = listOf("8.8.0", "8.9.0", "9.0.0", "9.5.0", "9.6.0")

    /** Versions selectable given [currentVersion]; equal-or-newer only, all versions when none is assigned yet. */
    fun selectableFrom(currentVersion: String?): List<String> {
        if (currentVersion.isNullOrBlank()) return KNOWN_VERSIONS
        val currentIndex = KNOWN_VERSIONS.indexOf(currentVersion)
        return if (currentIndex == -1) KNOWN_VERSIONS else KNOWN_VERSIONS.drop(currentIndex)
    }
}
