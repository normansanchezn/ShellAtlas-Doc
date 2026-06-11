import SwiftUI
import SD_Domain
import SD_DesignSystem
import DS_Core

public struct RootNavigationView: View {
    @Environment(\.appServices) private var services
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    @AppStorage(DocumentationAssistantPresentationConfiguration.hasCompletedOnboardingKey)
    private var hasCompletedAssistantOnboarding = false
    @AppStorage(DocumentationAssistantPresentationConfiguration.usesOllamaKey)
    private var usesOllamaAssistant = false
    @State private var assistantStatus: DocumentationAssistantStatus?

    public init() {}

    public var body: some View {
        Group {
            #if os(macOS) || os(visionOS)
            SidebarNavigationView()
            #else
            if horizontalSizeClass == .regular {
                SidebarNavigationView()
            } else {
                TabNavigationView()
            }
            #endif
        }
        .tint(SDColors.primary)
        .background(SDColors.background)
        #if os(macOS)
        .containerBackground(SDColors.background, for: .window)
        #endif
        .preferredColorScheme(.dark)
        .alert("Use Local Intelligent Assistant?", isPresented: assistantOnboardingBinding) {
            Button("Use Local Ollama") {
                usesOllamaAssistant = true
                hasCompletedAssistantOnboarding = true
            }
            Button("Use Demo Assistant", role: .cancel) {
                usesOllamaAssistant = false
                hasCompletedAssistantOnboarding = true
            }
        } message: {
            Text(onboardingMessage)
        }
        .task {
            guard assistantStatus == nil, let services else { return }
            assistantStatus = await services.checkDocumentationAssistantStatusUseCase.execute()
        }
    }

    private var onboardingMessage: String {
        guard let assistantStatus else {
            return "ShellDoc can answer from local documentation using Ollama on this Mac. Checking whether Ollama and \(DocumentationAssistantPresentationConfiguration.defaultOllamaModel) are already available..."
        }

        if assistantStatus.isReady {
            return "Ollama is already ready with \(assistantStatus.modelName). ShellDoc will use the existing local setup and will not install it again."
        }

        if assistantStatus.isOllamaRunning {
            return "Ollama is already running, but \(assistantStatus.modelName) is not installed. Install only the missing model before using local Ollama."
        }

        return "Ollama is not running on this Mac. Install and start Ollama first, then pull \(assistantStatus.modelName). ShellDoc will not reinstall anything that is already available."
    }

    private var assistantOnboardingBinding: Binding<Bool> {
        Binding(
            get: { !hasCompletedAssistantOnboarding },
            set: { newValue in
                if !newValue {
                    hasCompletedAssistantOnboarding = true
                }
            }
        )
    }
}

// MARK: - Tab (iPhone / compact)

private struct TabNavigationView: View {
    @Environment(\.appServices) private var services
    @State private var notificationState = OutdatedNotificationState()

    var body: some View {
        TabView {
            Tab(AppRoute.assistant.title, systemImage: AppRoute.assistant.systemImage) {
                NavigationStack {
                    AssistantView(notificationState: notificationState)
                }
            }
            Tab(AppRoute.explorer.title, systemImage: AppRoute.explorer.systemImage) {
                DocumentationExplorerView()
            }
            Tab(AppRoute.updatesPending.title, systemImage: AppRoute.updatesPending.systemImage) {
                NavigationStack {
                    OutdatedReviewView()
                }
            }
            Tab(AppRoute.dashboard.title, systemImage: AppRoute.dashboard.systemImage) {
                NavigationStack {
                    DashboardView()
                }
            }
            Tab("More", systemImage: "ellipsis.circle.fill") {
                NavigationStack {
                    MoreMenuView()
                }
            }
        }
        .onAppear {
            Task {
                guard let services else { return }
                try await notificationState.load(services: services)
            }
        }
    }
}

// MARK: - Sidebar (iPad / macOS)

private struct SidebarNavigationView: View {
    @Environment(\.appServices) private var services
    @State private var selectedRoute: AppRoute? = .assistant
    @State private var navigationPath = NavigationPath()
    @State private var notificationState = OutdatedNotificationState()
    @State private var showsLoggerPanel = false

    var body: some View {
        NavigationSplitView {
            List(selection: $selectedRoute) {
                Section("Knowledge") {
                    sidebarLink(.assistant)
                    sidebarLink(.explorer)
                    sidebarLink(.updatesPending)
                }
                Section("Analytics") {
                    sidebarLink(.dashboard)
                }
                Section("More") {
                    sidebarLink(.sources)
                    sidebarLink(.settings)
                }
            }
            .scrollContentBackground(.hidden)
            .background(SDColors.background)
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    #if os(macOS)
                    Button {
                        withAnimation(.snappy) { showsLoggerPanel.toggle() }
                    } label: {
                        Image(systemName: showsLoggerPanel ? "terminal.fill" : "terminal")
                            .font(.system(size: 13, weight: .semibold))
                    }
                    .help(showsLoggerPanel ? "Close connection logs" : "Open connection logs")
                    #else
                    Button {
                        showsLoggerPanel.toggle()
                    } label: {
                        Image(systemName: showsLoggerPanel ? "terminal.fill" : "terminal")
                            .font(.system(size: 13, weight: .semibold))
                    }
                    .help(showsLoggerPanel ? "Close connection logs" : "Open connection logs")
                    .popover(isPresented: $showsLoggerPanel) {
                        LoggerPanelView(isPresented: $showsLoggerPanel)
                            .frame(width: 440, height: 560)
                            .presentationCompactAdaptation(.none)
                    }
                    #endif
                }
            }
        } detail: {
            NavigationStack(path: $navigationPath) {
                detailView(for: selectedRoute ?? .assistant)
                    .navigationDestination(for: AppRoute.self) { route in
                        detailView(for: route)
                    }
            }
        }
        #if os(macOS)
        .overlay(alignment: .topTrailing) {
            if showsLoggerPanel {
                LoggerPanelView(isPresented: $showsLoggerPanel)
                    .frame(width: 460)
                    .padding(.top, 52)
                    .padding(.trailing, 12)
                    .transition(.move(edge: .trailing).combined(with: .opacity))
            }
        }
        .animation(.snappy, value: showsLoggerPanel)
        #endif
        .onAppear {
            Task {
                guard let services else { return }
                try await notificationState.load(services: services)
            }
        }
    }

    @ViewBuilder
    private func detailView(for route: AppRoute) -> some View {
        switch route {
        case .assistant:
            AssistantView(notificationState: notificationState)
        case .explorer:
            DocumentationExplorerView()
        case .updatesPending:
            OutdatedReviewView()
        case .dashboard:
            DashboardView()
        case .sources:
            MockSourcesView()
        case .settings:
            SettingsView()
        case .document(let id):
            DocumentByIDView(documentID: id)
        }
    }

    private func sidebarLink(_ route: AppRoute) -> some View {
        NavigationLink(value: route) {
            Label(route.title, systemImage: route.systemImage)
        }
    }
}

// MARK: - Document by ID (loads from container)

struct DocumentByIDView: View {
    @Environment(\.appServices) private var services
    let documentID: String
    @State private var document: KnowledgeDocument? = nil

    var body: some View {
        Group {
            if let doc = document {
                DocumentReaderView(document: doc)
            } else {
                ProgressView("Loading…")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .task {
            guard let services else { return }
            document = try? await services.getDocumentDetailUseCase.execute(id: documentID)
        }
    }
}

// MARK: - More Menu

struct MoreMenuView: View {
    var body: some View {
        List {
            NavigationLink {
                MockSourcesView()
            } label: {
                Label(AppRoute.sources.title, systemImage: AppRoute.sources.systemImage)
            }
            NavigationLink {
                SettingsView()
            } label: {
                Label(AppRoute.settings.title, systemImage: AppRoute.settings.systemImage)
            }
        }
        .navigationTitle("More")
    }
}
