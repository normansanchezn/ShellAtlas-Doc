package com.shelldocs.core.common.testing

object DemoTestTags {
    const val SignInRoot = "demo_sign_in_root"
    const val SignInEmail = "demo_sign_in_email"
    const val SignInPassword = "demo_sign_in_password"
    const val SignInSubmit = "demo_sign_in_submit"
    const val PasswordToggle = "demo_password_toggle"

    const val WorkspaceRoot = "demo_workspace_root"

    const val AssistantScreen = "demo_assistant_screen"
    const val AssistantInput = "demo_assistant_input"
    const val AssistantSend = "demo_assistant_send"

    const val DocumentsScreen = "demo_documents_screen"
    const val DocumentsNew = "demo_documents_new"
    const val DocumentsEdit = "demo_documents_edit"
    const val DocumentsHistory = "demo_documents_history"
    const val DocumentsBookmark = "demo_documents_bookmark"
    const val DocumentsNewTitle = "demo_documents_new_title"
    const val DocumentsNewMarkdown = "demo_documents_new_markdown"
    const val DocumentsCreate = "demo_documents_create"
    const val DocumentsEditorMarkdown = "demo_documents_editor_markdown"
    const val DocumentsSaveDraft = "demo_documents_save_draft"
    const val DocumentsPublish = "demo_documents_publish"

    const val DashboardScreen = "demo_dashboard_screen"
    const val DashboardRefresh = "demo_dashboard_refresh"

    const val UpdatesScreen = "demo_updates_screen"
    const val UpdatesScan = "demo_updates_scan"

    const val SourcesScreen = "demo_sources_screen"
    const val SettingsScreen = "demo_settings_screen"
    const val SettingsSignOut = "demo_settings_sign_out"

    fun updatesRisk(riskName: String): String =
        "demo_updates_risk_" + riskName.lowercase()

    fun sourceSync(sourceKind: String): String =
        "demo_source_sync_" + sourceKind.lowercase().replace(' ', '_')

    fun sourceReconnect(sourceKind: String): String =
        "demo_source_reconnect_" + sourceKind.lowercase().replace(' ', '_')

    fun settingsSection(sectionName: String): String =
        "demo_settings_section_" + sectionName.lowercase().replace(' ', '_').replace('&', '_')

    fun navRoute(routeTitle: String): String =
        "demo_nav_" + routeTitle.lowercase().replace(' ', '_')
}
