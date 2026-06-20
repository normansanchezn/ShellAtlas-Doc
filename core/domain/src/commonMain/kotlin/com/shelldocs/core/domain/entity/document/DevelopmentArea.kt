package com.shelldocs.core.domain.entity.document

/** Team/area a document belongs to; drives role-based notification visibility. */
enum class DevelopmentArea(val displayName: String) {
    DEVELOPMENT("Development"),
    BACKEND("Backend"),
    QA("QA"),
    DEVOPS("DevOps"),
    ARCHITECTURE("Architecture"),
    SECURITY("Security"),
    PRODUCT("Product"),
    ANALYTICS("Analytics"),
    OPERATIONS("Operations");

    companion object {
        fun fromKey(key: String?): DevelopmentArea? =
            entries.firstOrNull { it.name.equals(key, ignoreCase = true) || it.displayName.equals(key, ignoreCase = true) }
    }
}
