import Foundation
import SwiftUI
import SD_DesignSystem

struct SettingsView: View {
    @AppStorage(DocumentationAssistantPresentationConfiguration.usesOllamaKey)
    private var isAssistantEnabled = false
    @State private var themeMode: ThemeMode = .system
    @State private var compactMode = false
    @State private var reduceMotion = false
    @State private var defaultDocumentationSource: DocumentationSourcePreference = .allSources
    @State private var showArchivedDocuments = false
    @State private var showPendingUpdates = true
    @State private var highlightOutdatedDocumentation = true
    @State private var defaultSortOrder: DocumentationSortOrder = .recentlyUpdated
    @State private var useDocumentationContext = true
    @State private var showSourceReferences = true
    @State private var preferShortAnswers = false
    @State private var enableFollowUpSuggestions = true
    @State private var assistantResponseStyle: AssistantResponseStyle = .technical
    @State private var notifyNeedsReview = true
    @State private var notifyPendingUpdates = true
    @State private var notifyLinkedTicketChanges = false
    @State private var notifyReleaseNoteImpacts = true
    @State private var weeklyHealthSummary = true
    @State private var syncState: SyncActionState?
    @State private var confirmation: SettingsConfirmation?

    private let appVersion: String
    private let buildNumber: String
    private let modeLabel: String

    init(
        appVersion: String = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0.0",
        buildNumber: String = Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "MVP",
        isMockMode: Bool = true
    ) {
        self.appVersion = appVersion
        self.buildNumber = buildNumber
        self.modeLabel = isMockMode ? "Development" : "Production"
    }

    var body: some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 22) {
                profileSection
                appearanceSection
                documentationPreferencesSection
                assistantPreferencesSection
                syncAndDataSection
                integrationsSection
                notificationsSection
                aboutSection
            }
            .padding(24)
            .frame(maxWidth: 980)
            .frame(maxWidth: .infinity, alignment: .center)
        }
        .background(SDColors.background)
        .scrollContentBackground(.hidden)
        .navigationTitle("Settings")
        .preferredColorScheme(preferredColorScheme)
        .confirmationDialog(
            confirmation?.title ?? "",
            isPresented: Binding(
                get: { confirmation != nil },
                set: { if !$0 { confirmation = nil } }
            ),
            titleVisibility: .visible
        ) {
            if let confirmation {
                Button(confirmation.actionTitle, role: confirmation.role) {
                    perform(confirmation)
                    self.confirmation = nil
                }
                Button("Cancel", role: .cancel) {
                    self.confirmation = nil
                }
            }
        } message: {
            if let confirmation {
                Text(confirmation.message)
            }
        }
    }

    private var profileSection: some View {
        SDSettingsSection("Profile / Account", systemImage: "person.crop.circle") {
            SDSettingsRow(title: "User name", value: "Norman Sanchez", systemImage: "person.fill")
            SDSettingsDivider()
            SDSettingsRow(title: "Email", value: "norman.sanchez@shell.com", systemImage: "envelope.fill")
            SDSettingsDivider()
            SDSettingsRow(title: "Role", value: "Android Engineer", systemImage: "hammer.fill")
            SDSettingsDivider()
            SDSettingsRow(title: "Team", value: "Mobile Apps", systemImage: "person.3.fill")
            SDSettingsDivider()
            SDSettingsRow(title: "Environment", value: modeLabel, systemImage: "server.rack")
        }
    }

    private var appearanceSection: some View {
        SDSettingsSection("Appearance", systemImage: "paintpalette.fill") {
            SDSettingsPickerRow(title: "Theme mode", systemImage: "circle.lefthalf.filled", selection: $themeMode) {
                ForEach(ThemeMode.allCases) { mode in
                    Text(mode.rawValue).tag(mode)
                }
            }
            SDSettingsDivider()
            AccentPreviewRow()
            SDSettingsDivider()
            SDSettingsToggleRow(title: "Compact mode", subtitle: "Prepare denser layouts for repeated work.", systemImage: "rectangle.compress.vertical", isOn: $compactMode)
            SDSettingsDivider()
            SDSettingsToggleRow(title: "Reduce motion", subtitle: "Limit non-essential interface animation.", systemImage: "figure.walk.motion", isOn: $reduceMotion)
        }
    }

    private var documentationPreferencesSection: some View {
        SDSettingsSection("Documentation Preferences", systemImage: "doc.text.magnifyingglass") {
            SDSettingsPickerRow(title: "Default documentation source", systemImage: "tray.full.fill", selection: $defaultDocumentationSource) {
                ForEach(DocumentationSourcePreference.allCases) { source in
                    Text(source.rawValue).tag(source)
                }
            }
            SDSettingsDivider()
            SDSettingsToggleRow(title: "Show archived documents", systemImage: "archivebox.fill", isOn: $showArchivedDocuments)
            SDSettingsDivider()
            SDSettingsToggleRow(title: "Show documents with pending updates", systemImage: "clock.badge.exclamationmark.fill", isOn: $showPendingUpdates)
            SDSettingsDivider()
            SDSettingsToggleRow(title: "Highlight outdated documentation", systemImage: "exclamationmark.triangle.fill", isOn: $highlightOutdatedDocumentation)
            SDSettingsDivider()
            SDSettingsPickerRow(title: "Default sort order", systemImage: "arrow.up.arrow.down", selection: $defaultSortOrder) {
                ForEach(DocumentationSortOrder.allCases) { order in
                    Text(order.rawValue).tag(order)
                }
            }
        }
    }

    private var assistantPreferencesSection: some View {
        SDSettingsSection("Assistant Preferences", systemImage: "brain.head.profile") {
            SDSettingsToggleRow(title: "Enable assistant", subtitle: "Use ShellDoc's local documentation assistant.", systemImage: "power.circle.fill", isOn: $isAssistantEnabled)
            SDSettingsDivider()
            SDSettingsToggleRow(title: "Use documentation context", subtitle: "Answers stay grounded in retrieved ShellDoc content.", systemImage: "doc.richtext.fill", isOn: $useDocumentationContext)
            SDSettingsDivider()
            SDSettingsToggleRow(title: "Show source references", systemImage: "link.circle.fill", isOn: $showSourceReferences)
            SDSettingsDivider()
            SDSettingsToggleRow(title: "Prefer short answers", systemImage: "text.quote", isOn: $preferShortAnswers)
            SDSettingsDivider()
            SDSettingsToggleRow(title: "Enable follow-up suggestions", systemImage: "sparkles", isOn: $enableFollowUpSuggestions)
            SDSettingsDivider()
            SDSettingsPickerRow(title: "Assistant response style", systemImage: "slider.horizontal.3", selection: $assistantResponseStyle) {
                ForEach(AssistantResponseStyle.allCases) { style in
                    Text(style.rawValue).tag(style)
                }
            }
        }
    }

    private var syncAndDataSection: some View {
        SDSettingsSection("Sync and Data", systemImage: "arrow.triangle.2.circlepath") {
            SyncStatusRow(syncState: syncState)
            SDSettingsDivider()
            SDSettingsRow(title: "Last sync", value: "Today, 10:30 PM", systemImage: "calendar.badge.clock")
            SDSettingsDivider()
            SDSettingsActionRow(title: syncState == .syncing ? "Syncing..." : "Manual sync", subtitle: "Refresh mock source metadata.", systemImage: "arrow.clockwise") {
                runMockSync()
            }
            SDSettingsDivider()
            SDSettingsActionRow(title: "Clear local cache", subtitle: "Requires confirmation.", systemImage: "trash.fill", role: .destructive) {
                confirmation = .clearCache
            }
            SDSettingsDivider()
            SDSettingsActionRow(title: "Rebuild local index", subtitle: "Recreates the local retrieval index.", systemImage: "square.stack.3d.up.fill") {
                confirmation = .rebuildIndex
            }
        }
    }

    private var integrationsSection: some View {
        SDSettingsSection("Integrations", systemImage: "point.3.connected.trianglepath.dotted") {
            IntegrationStatusRow(name: "Confluence", systemImage: "doc.on.doc.fill", status: .connected)
            SDSettingsDivider()
            IntegrationStatusRow(name: "Jira", systemImage: "ticket.fill", status: .connected)
            SDSettingsDivider()
            IntegrationStatusRow(name: "GitHub", systemImage: "chevron.left.forwardslash.chevron.right", status: .connected)
            SDSettingsDivider()
            IntegrationStatusRow(name: "GitHub Actions", systemImage: "play.circle.fill", status: .syncPending)
            SDSettingsDivider()
            IntegrationStatusRow(name: "Azure DevOps", systemImage: "shippingbox.fill", status: .notConnected)
            SDSettingsDivider()
            IntegrationStatusRow(name: "Release Notes", systemImage: "megaphone.fill", status: .needsConfiguration)
        }
    }

    private var notificationsSection: some View {
        SDSettingsSection("Notifications", systemImage: "bell.badge.fill") {
            SDSettingsToggleRow(title: "Notify when documentation needs review", systemImage: "checklist.checked", isOn: $notifyNeedsReview)
            SDSettingsDivider()
            SDSettingsToggleRow(title: "Notify when updates are pending", systemImage: "clock.fill", isOn: $notifyPendingUpdates)
            SDSettingsDivider()
            SDSettingsToggleRow(title: "Notify when a linked ticket changes", systemImage: "ticket.fill", isOn: $notifyLinkedTicketChanges)
            SDSettingsDivider()
            SDSettingsToggleRow(title: "Notify when a release note impacts documentation", systemImage: "megaphone.fill", isOn: $notifyReleaseNoteImpacts)
            SDSettingsDivider()
            SDSettingsToggleRow(title: "Weekly documentation health summary", systemImage: "chart.bar.doc.horizontal.fill", isOn: $weeklyHealthSummary)
        }
    }

    private var aboutSection: some View {
        SDSettingsSection("About", systemImage: "info.circle.fill") {
            SDSettingsRow(title: "App name", value: "ShellDoc", systemImage: "app.fill")
            SDSettingsDivider()
            SDSettingsRow(title: "Version", value: appVersion, systemImage: "number")
            SDSettingsDivider()
            SDSettingsRow(title: "Build", value: buildNumber, systemImage: "hammer.fill")
            SDSettingsDivider()
            SDSettingsRow(title: "Environment", value: modeLabel, systemImage: "server.rack")
            SDSettingsDivider()
            SDSettingsActionRow(title: "Terms or internal policy", systemImage: "doc.plaintext.fill") {}
            SDSettingsDivider()
            SDSettingsActionRow(title: "Contact support", systemImage: "questionmark.circle.fill") {}
        }
    }

    private var preferredColorScheme: ColorScheme? {
        switch themeMode {
        case .system: nil
        case .light: .light
        case .dark: .dark
        }
    }

    private func runMockSync() {
        guard syncState != .syncing else { return }
        syncState = .syncing
        Task { @MainActor in
            try? await Task.sleep(for: .seconds(1))
            syncState = .synced
        }
    }

    private func perform(_ confirmation: SettingsConfirmation) {
        switch confirmation {
        case .clearCache:
            syncState = .cacheCleared
        case .rebuildIndex:
            syncState = .indexRebuilt
        }
    }
}

private struct AccentPreviewRow: View {
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "swatchpalette.fill")
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(SDColors.secondary)
                .frame(width: 26, height: 26)
                .background(SDColors.secondary.opacity(0.12), in: .rect(cornerRadius: 6))

            Text("Accent color preview")
                .font(.callout.weight(.medium))
                .foregroundStyle(SDColors.textPrimary)
            Spacer()
            HStack(spacing: 6) {
                Circle().fill(SDColors.primary)
                Circle().fill(SDColors.secondary)
                Circle().fill(SDColors.success)
            }
            .frame(width: 70, height: 18)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 11)
    }
}

private struct SyncStatusRow: View {
    let syncState: SyncActionState?

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: syncState == .syncing ? "arrow.clockwise" : "checkmark.seal.fill")
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(SDColors.success)
                .frame(width: 26, height: 26)
                .background(SDColors.success.opacity(0.12), in: .rect(cornerRadius: 6))
            Text("Sync status")
                .font(.callout.weight(.medium))
                .foregroundStyle(SDColors.textPrimary)
            Spacer()
            SDSettingsStatusBadge(syncTitle, style: syncState == .syncing ? .info : .success)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 11)
    }

    private var syncTitle: String {
        switch syncState {
        case .syncing: "Syncing"
        case .cacheCleared: "Cache cleared"
        case .indexRebuilt: "Index rebuilt"
        case .synced, .none: "Up to date"
        }
    }
}

private struct IntegrationStatusRow: View {
    let name: String
    let systemImage: String
    let status: IntegrationStatus

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: systemImage)
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(SDColors.secondary)
                .frame(width: 26, height: 26)
                .background(SDColors.secondary.opacity(0.12), in: .rect(cornerRadius: 6))
            Text(name)
                .font(.callout.weight(.medium))
                .foregroundStyle(SDColors.textPrimary)
            Spacer()
            SDSettingsStatusBadge(status.rawValue, style: status.badgeStyle)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 11)
    }
}

private enum ThemeMode: String, CaseIterable, Identifiable {
    case system = "System"
    case light = "Light"
    case dark = "Dark"

    var id: String { rawValue }
}

private enum DocumentationSourcePreference: String, CaseIterable, Identifiable {
    case allSources = "All sources"
    case confluence = "Confluence"
    case localMarkdown = "Local Markdown"
    case jira = "Jira"
    case github = "GitHub"

    var id: String { rawValue }
}

private enum DocumentationSortOrder: String, CaseIterable, Identifiable {
    case recentlyUpdated = "Recently updated"
    case title = "Title"
    case status = "Status"
    case source = "Source"

    var id: String { rawValue }
}

private enum AssistantResponseStyle: String, CaseIterable, Identifiable {
    case concise = "Concise"
    case technical = "Technical"
    case detailed = "Detailed"

    var id: String { rawValue }
}

private enum IntegrationStatus: String {
    case connected = "Connected"
    case notConnected = "Not connected"
    case needsConfiguration = "Needs configuration"
    case syncPending = "Sync pending"

    var badgeStyle: SDSettingsStatusBadge.Style {
        switch self {
        case .connected: .success
        case .notConnected: .muted
        case .needsConfiguration: .warning
        case .syncPending: .info
        }
    }
}

private enum SyncActionState {
    case syncing
    case synced
    case cacheCleared
    case indexRebuilt
}

private enum SettingsConfirmation: Identifiable {
    case clearCache
    case rebuildIndex

    var id: String { title }

    var title: String {
        switch self {
        case .clearCache: "Clear local cache?"
        case .rebuildIndex: "Rebuild local index?"
        }
    }

    var message: String {
        switch self {
        case .clearCache:
            "This clears locally cached mock data. Backend cache persistence can be connected later."
        case .rebuildIndex:
            "This rebuilds the local documentation retrieval index using the available mock documents."
        }
    }

    var actionTitle: String {
        switch self {
        case .clearCache: "Clear cache"
        case .rebuildIndex: "Rebuild index"
        }
    }

    var role: ButtonRole? {
        switch self {
        case .clearCache: .destructive
        case .rebuildIndex: nil
        }
    }
}

#Preview {
    NavigationStack {
        SettingsView()
    }
}
