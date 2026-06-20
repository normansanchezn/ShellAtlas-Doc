package com.shelldocs.app.navigation

/** Top-level destinations of the workspace, mirroring the sidebar. */
enum class AppRoute(val title: String) {
    ASSISTANT("AI Assistant"),
    DOCUMENTS("Documents"),
    UPDATES("Updates Pending"),
    DASHBOARD("Dashboard"),
    SOURCES("Sources"),
    SETTINGS("Settings"),
    AI_UPDATE("AI Suggested Update"),
}
