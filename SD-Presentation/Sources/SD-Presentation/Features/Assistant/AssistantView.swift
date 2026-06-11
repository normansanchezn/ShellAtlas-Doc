import SwiftUI
import SD_Domain
import SD_DesignSystem

struct AssistantView: View {
    @Environment(\.appServices) private var services
    @AppStorage(DocumentationAssistantPresentationConfiguration.usesOllamaKey)
    private var usesOllamaAssistant = false
    @State private var viewModel = AssistantViewModel()
    @State private var showsAssistantInfo = false
    var notificationState: OutdatedNotificationState
    private let bottomAnchorID = "assistant-message-list-bottom"

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            assistantWorkspace

            NotificationFAB(state: notificationState)
        }
        .navigationTitle("")
        .task(id: "banner-collapse") {
            try? await Task.sleep(for: .seconds(8))
            if notificationState.isExpanded {
                notificationState.collapse()
            }
        }
        .task(id: usesOllamaAssistant) {
            guard let services else { return }
            await viewModel.refreshStatus(
                useCase: services.checkDocumentationAssistantStatusUseCase,
                usesOllamaAssistant: usesOllamaAssistant
            )
        }
        .background(SDColors.background)
    }

    private var assistantWorkspace: some View {
        HStack(spacing: 0) {
            VStack(spacing: 0) {
                if notificationState.isExpanded {
                    NotificationBannerView(state: notificationState)
                }

                header
                messageList
                Divider()
                    .overlay(SDColors.border.opacity(0.7))
                SDAssistantInputBar(
                    text: Bindable(viewModel).inputText,
                    placeholder: viewModel.responseLanguage == .spanish ? "Pregunta sobre la documentación local..." : "Ask about local documentation...",
                    isSending: viewModel.isThinking
                ) {
                    send()
                }
            }

            if viewModel.selectedSource != nil {
                Divider()
                    .overlay(SDColors.border.opacity(0.7))
                selectedSourcePanel
                    .frame(minWidth: 340, idealWidth: 420, maxWidth: 520)
                    .transition(.move(edge: .trailing).combined(with: .opacity))
            }
        }
        .animation(.easeOut(duration: 0.2), value: viewModel.selectedSource?.id)
    }

    private var header: some View {
        HStack(spacing: 14) {
            VStack(alignment: .leading, spacing: 4) {
                Text("Documentation Assistant")
                    .font(.title3.weight(.semibold))
                    .foregroundStyle(SDColors.textPrimary)

                Text("Grounded answers from ShellDoc knowledge")
                    .font(.caption)
                    .foregroundStyle(SDColors.textSecondary)
            }

            Spacer()

            Button {
                retryLocalModelConnection()
            } label: {
                SDLocalModelStatusBadge(
                    title: statusBadgeTitle,
                    isAvailable: viewModel.isLocalModelAvailable
                )
            }
            .buttonStyle(.plain)
            .disabled(viewModel.isCheckingLocalModel || viewModel.isLocalModelAvailable)
            .help(statusBadgeHelp)

            Button {
                showsAssistantInfo.toggle()
            } label: {
                Image(systemName: "info.circle")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundStyle(SDColors.secondary)
                    .frame(width: 30, height: 30)
                    .contentShape(.rect)
            }
            .buttonStyle(.plain)
            .help("Assistant information")
            .popover(isPresented: $showsAssistantInfo, arrowEdge: .bottom) {
                assistantInfoPopover
            }

            Button {
                viewModel.clearConversation()
            } label: {
                Image(systemName: "trash")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundStyle(viewModel.messages.isEmpty ? SDColors.textSecondary.opacity(0.5) : SDColors.textSecondary)
                    .frame(width: 30, height: 30)
                    .contentShape(.rect)
            }
            .buttonStyle(.plain)
            .disabled(viewModel.messages.isEmpty || viewModel.isThinking)
            .help("Clear conversation")
        }
        .help(viewModel.statusMessage)
        .padding(.horizontal, 20)
        .padding(.vertical, 14)
        .background(SDColors.surface.opacity(0.86))
        .overlay(alignment: .bottom) {
            Rectangle()
                .fill(SDColors.border.opacity(0.65))
                .frame(height: 1)
        }
    }

    private var statusBadgeTitle: String {
        switch viewModel.availabilityState {
        case .checking: return "Verificando AI…"
        case .available: return "AI disponible"
        case .ollamaNotRunning: return "Ollama no disponible"
        case .modelNotInstalled: return "Modelo no instalado"
        case .unavailable: return "AI no disponible"
        }
    }

    private var statusBadgeHelp: String {
        viewModel.isLocalModelAvailable
            ? viewModel.statusMessage
            : "Haz clic para reintentar la conexión AI local."
    }

    private var assistantInfoPopover: some View {
        VStack(alignment: .leading, spacing: 14) {
            Label("ShellDoc Assistant", systemImage: "brain.head.profile")
                .font(.headline)
                .foregroundStyle(SDColors.textPrimary)

            assistantPopoverStatusSection

            DisclosureGroup("Detalles técnicos") {
                VStack(alignment: .leading, spacing: 4) {
                    popoverDetailRow("Modo", usesOllamaAssistant ? "Local Ollama" : "Demo local mock")
                    popoverDetailRow("Modelo", usesOllamaAssistant ? DocumentationAssistantConfiguration.defaultOllamaModel : "Deterministic mock")
                    popoverDetailRow("Base URL", DocumentationAssistantConfiguration.defaultOllamaBaseURL)
                    popoverDetailRow("Endpoint", "/api/tags")
                }
                .padding(.top, 6)
            }
            .font(.caption)
            .foregroundStyle(SDColors.textSecondary)
        }
        .padding(16)
        .frame(width: 320, alignment: .leading)
        .background(.regularMaterial)
    }

    @ViewBuilder
    private var assistantPopoverStatusSection: some View {
        switch viewModel.availabilityState {
        case .checking:
            popoverStatusBody(
                message: "Verificando disponibilidad del AI local…",
                detail: nil
            )
        case .available:
            popoverStatusBody(
                message: "Local AI is ready.",
                detail: "Answers are generated using indexed ShellDoc documentation and may include source references when available."
            )
        case .ollamaNotRunning:
            popoverStatusBody(
                message: "Local AI is not available right now.",
                detail: "Start Ollama and try again."
            )
        case .modelNotInstalled(let model):
            popoverStatusBody(
                message: "The local AI service is running, but the configured model is not installed.",
                detail: "Install the model and try again.\n\nollama pull \(model)"
            )
        case .unavailable(let reason):
            popoverStatusBody(
                message: "Local AI is not available.",
                detail: reason
            )
        }
    }

    private func popoverStatusBody(message: String, detail: String?) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(message)
                .font(.callout.weight(.medium))
                .foregroundStyle(SDColors.textPrimary)
                .fixedSize(horizontal: false, vertical: true)
            if let detail {
                Text(detail)
                    .font(.caption)
                    .foregroundStyle(SDColors.textSecondary)
                    .fixedSize(horizontal: false, vertical: true)
            }
        }
    }

    private func popoverDetailRow(_ label: String, _ value: String) -> some View {
        HStack(alignment: .top, spacing: 6) {
            Text(label + ":")
                .foregroundStyle(SDColors.textSecondary)
            Text(value)
                .foregroundStyle(SDColors.textPrimary)
                .textSelection(.enabled)
        }
        .font(.caption)
    }

    private var messageList: some View {
        ScrollViewReader { proxy in
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 12) {
                    ForEach(viewModel.messages) { message in
                        SDAssistantMessageBubble(
                            role: message.role.designSystemRole,
                            text: message.text,
                            sources: message.sources.map(\.designSystemSource),
                            selectedSourceID: viewModel.selectedSource?.id,
                            sourcesTitle: viewModel.responseLanguage.sourcesTitle,
                            openSourceAccessibilityPrefix: viewModel.responseLanguage.openSourceAccessibilityPrefix,
                            assistantImageName: "shell_doc_icon",
                            onSourceSelected: { source in
                                selectSource(source)
                            }
                        )
                        .id(message.id)
                    }

                    if viewModel.isThinking {
                        SDTypingIndicator(assistantImageName: "shell_doc_icon")
                    }

                    Color.clear
                        .frame(height: 1)
                        .id(bottomAnchorID)
                }
                .padding()
            }
            .background(SDColors.background.opacity(0.94))
            .overlay {
                if viewModel.messages.isEmpty {
                    SDAssistantEmptyState(
                        title: "Ask ShellDoc anything",
                        message: "Understand docs, decode acronyms, map processes and find gaps in internal knowledge.",
                        ctaTitle: "Start asking"
                    ) {
                        scrollToBottom(proxy)
                    }
                }
            }
            .onChange(of: viewModel.messages.count) {
                scrollToBottom(proxy)
            }
            .onChange(of: viewModel.isThinking) {
                scrollToBottom(proxy)
            }
        }
    }

    private var selectedSourcePanel: some View {
        VStack(spacing: 0) {
            HStack(spacing: 10) {
                Image(systemName: "doc.text.fill")
                    .foregroundStyle(SDColors.secondary)
                Text(viewModel.selectedSource?.title ?? viewModel.responseLanguage.sourceDocumentTitle)
                    .font(.headline.weight(.semibold))
                    .foregroundStyle(SDColors.textPrimary)
                    .lineLimit(1)
                Spacer()
                Button {
                    viewModel.closeSelectedSource()
                } label: {
                    Image(systemName: "xmark.circle.fill")
                        .font(.title3)
                        .foregroundStyle(SDColors.textSecondary)
                }
                .buttonStyle(.plain)
                .help("Close document")
            }
            .padding(.horizontal, 18)
            .padding(.vertical, 14)
            .background(.regularMaterial)

            Divider()
                .overlay(SDColors.border.opacity(0.7))

            Group {
                if viewModel.isLoadingSelectedSource {
                    SDLottieLoadingView(
                        viewModel.responseLanguage.loadingSourceTitle,
                        message: viewModel.responseLanguage.loadingSourceMessage
                    )
                } else if let message = viewModel.selectedSourceErrorMessage {
                    SourceUnavailablePlaceholder(
                        title: viewModel.responseLanguage.sourceUnavailableTitle,
                        message: message,
                        retryTitle: viewModel.responseLanguage.retryTitle
                    ) {
                        if let source = viewModel.selectedSource {
                            selectSource(source.designSystemSource)
                        }
                    }
                } else if let document = viewModel.selectedSourceDocument {
                    ScrollView {
                        MarkdownBodyView(content: document.content)
                            .padding(22)
                    }
                    .scrollContentBackground(.hidden)
                } else {
                    SDEmptyStateView("Select a source", systemImage: "doc.text.magnifyingglass", message: "Choose a source from an assistant answer.")
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .background(.regularMaterial)
    }

    private func scrollToBottom(_ proxy: ScrollViewProxy) {
        Task { @MainActor in
            await Task.yield()
            withAnimation(.easeOut(duration: 0.2)) {
                proxy.scrollTo(bottomAnchorID, anchor: .bottom)
            }
        }
    }

    private func send() {
        guard let services else { return }
        Task {
            await viewModel.send(useCase: services.askDocumentationAssistantUseCase)
        }
    }

    private func selectSource(_ source: SDAssistantSource) {
        guard let services else { return }
        let documentationSource = DocumentationSource(
            id: source.id,
            documentId: source.documentId,
            title: source.title,
            sectionTitle: source.sectionTitle
        )
        Task {
            await viewModel.selectSource(
                documentationSource,
                useCase: services.getDocumentationDocumentDetailUseCase
            )
        }
    }

    private func retryLocalModelConnection() {
        guard let services else { return }
        Task {
            await viewModel.refreshStatus(
                useCase: services.checkDocumentationAssistantStatusUseCase,
                usesOllamaAssistant: usesOllamaAssistant
            )
        }
    }
}

private struct SourceUnavailablePlaceholder: View {
    let title: String
    let message: String
    let retryTitle: String
    let retry: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "doc.badge.exclamationmark")
                .font(.system(size: 34, weight: .semibold))
                .foregroundStyle(SDColors.warning)
                .frame(width: 64, height: 64)
                .background(SDColors.warningSoft, in: .rect(cornerRadius: 16))

            VStack(spacing: 7) {
                Text(title)
                    .font(.headline.weight(.semibold))
                    .foregroundStyle(SDColors.textPrimary)
                Text(message)
                    .font(.callout)
                    .foregroundStyle(SDColors.textSecondary)
                    .multilineTextAlignment(.center)
                    .fixedSize(horizontal: false, vertical: true)
                    .frame(maxWidth: 360)
            }

            SDPrimaryButton(retryTitle, systemImage: "arrow.clockwise", action: retry)
        }
        .padding(24)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(SDColors.contentBackground)
        .accessibilityElement(children: .combine)
    }
}

#Preview {
    NavigationStack {
        AssistantView(notificationState: OutdatedNotificationState())
    }
}
