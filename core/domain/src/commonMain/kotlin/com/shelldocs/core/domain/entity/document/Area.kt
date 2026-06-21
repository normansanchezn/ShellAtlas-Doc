package com.shelldocs.core.domain.entity.document

/** Fixed, admin-selectable area a document belongs to; drives role-based notification visibility. */
enum class Area(val displayName: String) {
    DEVELOPMENT("Development"),
    BUSINESS("Business"),
    SHELL("Shell"),
    DESIGN("Design"),
    MANAGEMENT("Management"),
    ARCHITECTURE("Architecture"),
    PI("PI"),
    QA("QA"),
    OWNERS("Owners"),
    PRODUCT("Producto");

    companion object {
        fun fromKey(key: String?): Area? =
            entries.firstOrNull {
                it.name.equals(key, ignoreCase = true) || it.displayName.equals(
                    key,
                    ignoreCase = true
                )
            }
    }
}
