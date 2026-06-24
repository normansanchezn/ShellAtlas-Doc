package com.shelldocs.app.navigation

import com.shelldocs.core.designsystem.i18n.AppStrings

/**
 * Top-level destinations of the workspace, mirroring the sidebar.
 * [title] is a stable English identifier (test tags, accessibility logs) — it never changes with
 * the user's language. For on-screen text use [label], which reads from [AppStrings].
 */
enum class AppRoute(val title: String) {
    ASSISTANT("AI Assistant"),
    DOCUMENTS("Documents"),
    UPDATES("Alerts"),
    DASHBOARD("Dashboard"),
    SOURCES("Sources"),
    SETTINGS("Settings"),
    AI_UPDATE("AI Suggested Update"),
}

/** Localized display label for [route] — use this for any UI text, never [AppRoute.title]. */
fun AppRoute.label(strings: AppStrings): String = when (this) {
    AppRoute.ASSISTANT -> strings.navAssistant
    AppRoute.DOCUMENTS -> strings.navDocuments
    AppRoute.UPDATES -> strings.navAlerts
    AppRoute.DASHBOARD -> strings.navDashboard
    AppRoute.SOURCES -> strings.navSources
    AppRoute.SETTINGS -> strings.navSettings
    AppRoute.AI_UPDATE -> title
}
