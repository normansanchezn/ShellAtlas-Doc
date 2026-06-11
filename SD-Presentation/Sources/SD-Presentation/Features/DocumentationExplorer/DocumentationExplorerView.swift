import SwiftUI
import SD_DesignSystem
import SD_Domain

public struct DocumentationExplorerView: View {
    @Environment(\.appServices) private var services
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    @State private var viewModel = DocumentationExplorerViewModel()
    @State private var showsAttributesPanel = false
    @State private var compactNavigationID: UUID?
    @State private var compactSelectedDocumentID: String?

    public init() {}

    public var body: some View {
        @Bindable var viewModel = viewModel

        NavigationStack {
            VStack(spacing: 0) {
                HStack(spacing: 12) {
                    SDSearchField(text: $viewModel.searchQuery, placeholder: "Search documents...", onClear: {
                        viewModel.clearSearch()
                    })
                    .frame(maxWidth: .infinity)

                    if horizontalSizeClass == .compact {
                        SDGlassIconButton(systemImage: "plus", accessibilityLabel: "New Document") {
                            viewModel.createNewDocument()
                        }
                    } else {
                        SDPrimaryButton("New Document", systemImage: "plus") {
                            viewModel.createNewDocument()
                        }
                        .layoutPriority(1)
                    }
                }
                .padding(.horizontal, 18)
                .padding(.vertical, 12)
                .background(SDColors.topBarBackground)
                .overlay(alignment: .bottom) {
                    Rectangle()
                        .fill(SDColors.border.opacity(0.65))
                        .frame(height: 1)
                }

                Group {
                    if horizontalSizeClass == .compact {
                        DocumentationTreePanel(viewModel: viewModel, onDocumentSelected: { id in
                            compactSelectedDocumentID = id
                            compactNavigationID = UUID()
                        })
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                    } else {
                        HStack(spacing: 0) {
                            DocumentationTreePanel(viewModel: viewModel)
                                .frame(minWidth: 220, idealWidth: 280, maxWidth: 340)

                            Divider()

                            ZStack(alignment: .topTrailing) {
                                DocumentationContentPanel(viewModel: viewModel)
                                    .frame(maxWidth: .infinity, maxHeight: .infinity)

                                if !showsAttributesPanel {
                                    SDGlassIconButton(systemImage: "sidebar.right", accessibilityLabel: "Show attributes") {
                                        withAnimation(.snappy) {
                                            showsAttributesPanel = true
                                        }
                                    }
                                    .padding(.top, 16)
                                    .padding(.trailing, 18)
                                    .transition(.opacity.combined(with: .scale(scale: 0.96)))
                                }
                            }
                                .frame(maxWidth: .infinity, maxHeight: .infinity)

                            if showsAttributesPanel {
                                Divider()

                                DocumentationAttributesPanel(viewModel: viewModel) {
                                    withAnimation(.snappy) {
                                        showsAttributesPanel = false
                                    }
                                }
                                    .frame(minWidth: 280, idealWidth: 320, maxWidth: 380)
                                    .transition(.move(edge: .trailing).combined(with: .opacity))
                            }
                        }
                    }
                }
            }
            .background(SDColors.background)
            .animation(.snappy, value: showsAttributesPanel)
            .navigationDestination(item: $viewModel.editorMode) { mode in
                DocumentationEditorView(mode: mode) { documentID in
                    Task {
                        await viewModel.handleEditorSave(documentID: documentID)
                    }
                }
            }
            .navigationDestination(item: $compactNavigationID) { _ in
                CompactDocumentDetailView(documentID: compactSelectedDocumentID, viewModel: viewModel)
            }
        }
        .task {
            guard let services else { return }
            viewModel.configure(services: services)
            await viewModel.onAppear()
        }
        .task(id: viewModel.searchQuery) {
            try? await Task.sleep(for: .milliseconds(220))
            guard !Task.isCancelled else { return }
            await viewModel.searchByTitle()
        }
        .alert("Rename", isPresented: Binding(
            get: { viewModel.renameTarget != nil },
            set: { if !$0 { viewModel.renameTarget = nil } }
        )) {
            TextField("Name", text: $viewModel.renameText)
            Button("Rename") {
                Task { await viewModel.commitRename() }
            }
            Button("Cancel", role: .cancel) {
                viewModel.renameTarget = nil
            }
        }
        .alert(deleteDialogTitle, isPresented: Binding(
            get: { viewModel.deleteTarget != nil },
            set: { if !$0 { viewModel.deleteTarget = nil } }
        )) {
            Button("Delete", role: .destructive) {
                Task { await viewModel.confirmDelete() }
            }
            Button("Cancel", role: .cancel) {
                viewModel.deleteTarget = nil
            }
        } message: {
            Text(deleteDialogMessage)
        }
    }

    private var deleteDialogTitle: String {
        switch viewModel.deleteTarget {
        case .folder: "Delete folder?"
        case .document: "Delete document?"
        case nil: "Delete?"
        }
    }

    private var deleteDialogMessage: String {
        switch viewModel.deleteTarget {
        case .folder:
            return "This will delete the folder and all documents inside it. This action cannot be undone."
        case .document(_, let title):
            let displayTitle = title.count > 60 ? String(title.prefix(57)) + "…" : title
            return "Are you sure you want to delete \"\(displayTitle)\"? This cannot be undone."
        case nil:
            return ""
        }
    }
}

private struct DocumentationTreePanel: View {
    @Bindable var viewModel: DocumentationExplorerViewModel
    var onDocumentSelected: ((String) -> Void)? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            TreeFiltersView(viewModel: viewModel)
            .padding(.top, 16)
            .padding(.bottom, 10)

            Divider()

            Group {
            if viewModel.isLoadingTree {
                    SDLottieLoadingView("Loading tree...", message: "Preparing the documentation structure.")
                } else if viewModel.isSearching {
                    SearchResultsList(viewModel: viewModel, onDocumentSelected: onDocumentSelected)
                } else if viewModel.visibleTree.isEmpty {
                    if viewModel.filters.isActive {
                        FilterEmptyState {
                            viewModel.clearFilters()
                        }
                    } else if viewModel.errorMessage != nil {
                        VStack(spacing: 16) {
                            SDEmptyStateView(
                                "No documents to show",
                                systemImage: "network.slash",
                                message: "Could not connect to the documentation service. Make sure the backend is running and try again."
                            )
                            Button("Retry") {
                                Task { await viewModel.refresh() }
                            }
                            .buttonStyle(.borderedProminent)
                        }
                    } else {
                        SDEmptyStateView(
                            "No documents to show",
                            systemImage: "doc.text",
                            message: "Create your first document to get started."
                        )
                    }
                } else {
                    ScrollView {
                        LazyVStack(alignment: .leading, spacing: 2) {
                            ForEach(viewModel.visibleTree) { node in
                                DocumentationNodeRows(node: node, depth: 0, viewModel: viewModel, onDocumentSelected: onDocumentSelected)
                            }
                        }
                        .padding(.horizontal, 8)
                        .padding(.vertical, 10)
                    }
                }
            }
        }
        .background(SDColors.sidebarBackground.opacity(0.92))
    }
}

private struct TreeFiltersView: View {
    @Bindable var viewModel: DocumentationExplorerViewModel

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Text(viewModel.isSearching ? "Search Results" : "Filters")
                    .font(.caption.weight(.bold))
                    .foregroundStyle(SDColors.textMuted)
                    .textCase(.uppercase)
                Spacer()
                if viewModel.filters.isActive {
                    Button("Clear") {
                        viewModel.clearFilters()
                    }
                    .buttonStyle(.borderless)
                    .font(.caption.weight(.semibold))
                }
            }

            VStack(spacing: 7) {
                FilterMenu(title: "Module", value: viewModel.filters.module) {
                    Picker("Module", selection: $viewModel.filters.module) {
                        ForEach(viewModel.moduleOptions, id: \.self) { option in
                            Text(option).tag(option)
                        }
                    }
                }
                FilterMenu(title: "Platform", value: viewModel.filters.platform) {
                    Picker("Platform", selection: $viewModel.filters.platform) {
                        ForEach(viewModel.platformOptions, id: \.self) { option in
                            Text(option).tag(option)
                        }
                    }
                }
                FilterMenu(title: "Team", value: viewModel.filters.team) {
                    Picker("Team", selection: $viewModel.filters.team) {
                        ForEach(viewModel.teamOptions, id: \.self) { option in
                            Text(option).tag(option)
                        }
                    }
                }
                FilterMenu(title: "Status", value: viewModel.filters.status?.displayName ?? "All") {
                    Picker("Status", selection: $viewModel.filters.status) {
                        Text("All").tag(DocumentationStatus?.none)
                        ForEach(DocumentationStatus.allCases, id: \.self) { status in
                            Text(status.displayName).tag(DocumentationStatus?.some(status))
                        }
                    }
                }
            }
        }
        .padding(.horizontal, 14)
    }
}

private struct FilterMenu<Content: View>: View {
    let title: String
    let value: String
    @ViewBuilder let content: Content

    var body: some View {
        Menu {
            content
        } label: {
            HStack(spacing: 8) {
                Text(title)
                    .foregroundStyle(SDColors.textMuted)
                Spacer(minLength: 8)
                Text(value)
                    .foregroundStyle(SDColors.textPrimary)
                    .lineLimit(1)
                Image(systemName: "chevron.down")
                    .font(.caption2.weight(.bold))
                    .foregroundStyle(SDColors.textMuted)
            }
            .font(.caption.weight(.semibold))
            .padding(.horizontal, 10)
            .frame(height: 28)
            .background(SDColors.elevatedSurface.opacity(0.62), in: .rect(cornerRadius: 7))
            .overlay {
                RoundedRectangle(cornerRadius: 7)
                    .strokeBorder(SDColors.border.opacity(0.55), lineWidth: 1)
            }
        }
        .menuStyle(.button)
        .buttonStyle(.plain)
    }
}

private struct SearchResultsList: View {
    @Bindable var viewModel: DocumentationExplorerViewModel
    var onDocumentSelected: ((String) -> Void)? = nil

    var body: some View {
        Group {
            if viewModel.searchResults.isEmpty {
                SDEmptyStateView("No Matches", systemImage: "magnifyingglass", message: "Title search did not find any documents.")
            } else {
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: 2) {
                        ForEach(viewModel.searchResults) { document in
                            SDTreeNodeRow(
                                title: document.title,
                                systemImage: "doc.text",
                                depth: 0,
                                isSelected: viewModel.selectedDocumentID == document.id
                            ) {
                                onDocumentSelected?(document.id)
                                Task {
                                    await viewModel.selectDocument(id: document.id)
                                }
                            }
                        }
                    }
                    .padding(.horizontal, 8)
                    .padding(.vertical, 10)
                }
            }
        }
    }
}

private struct DocumentationNodeRows: View {
    let node: DocumentationNode
    let depth: Int
    @Bindable var viewModel: DocumentationExplorerViewModel
    var onDocumentSelected: ((String) -> Void)? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            switch node.type {
            case .folder:
                SDTreeNodeRow(
                    title: viewModel.displayTitle(for: node),
                    systemImage: "folder.fill",
                    depth: depth,
                    isSelected: viewModel.selectedFolderID == node.id,
                    isExpanded: viewModel.expandedNodeIDs.contains(node.id)
                ) {
                    withAnimation(.spring(duration: 0.2)) {
                        viewModel.toggleNode(node)
                    }
                }
                .contextMenu {
                    Button("Create New Document") {
                        viewModel.createNewDocument(in: node.id)
                    }
                    Button("Rename Folder") {
                        viewModel.requestRenameFolder(node)
                    }
                    Button("Delete Folder", role: .destructive) {
                        viewModel.requestDeleteFolder(node)
                    }
                }

                if viewModel.expandedNodeIDs.contains(node.id) {
                    ForEach(node.children) { child in
                        DocumentationNodeRows(node: child, depth: depth + 1, viewModel: viewModel, onDocumentSelected: onDocumentSelected)
                    }
                }
            case .document:
                SDTreeNodeRow(
                    title: node.title,
                    systemImage: "doc.text",
                    depth: depth,
                    isSelected: viewModel.selectedDocumentID == node.documentId
                ) {
                    guard let documentID = node.documentId else { return }
                    onDocumentSelected?(documentID)
                    Task {
                        await viewModel.selectDocument(id: documentID)
                    }
                }
                .contextMenu {
                    Button("Rename Document") {
                        viewModel.requestRenameDocument(node)
                    }
                    Button("Duplicate Document") {
                        Task { await viewModel.duplicateDocument(node) }
                    }
                    Button("Delete Document", role: .destructive) {
                        viewModel.requestDeleteDocument(node)
                    }
                }
            }
        }
    }
}

private struct DocumentationContentPanel: View {
    @Bindable var viewModel: DocumentationExplorerViewModel

    var body: some View {
        Group {
            if viewModel.isLoadingDocument {
                SDLottieLoadingView("Loading document...", message: "Opening the selected document content.")
            } else if let errorMessage = viewModel.errorMessage {
                SDErrorStateView(message: errorMessage) {
                    Task {
                        await viewModel.refresh()
                    }
                }
            } else if let document = viewModel.selectedDocument {
                ScrollView {
                    VStack(alignment: .leading, spacing: 18) {
                        VStack(alignment: .leading, spacing: 8) {
                            Text(document.title)
                                .font(.largeTitle.weight(.bold))
                            if !document.summary.isEmpty {
                                Text(document.summary)
                                    .font(.title3)
                                    .foregroundStyle(SDColors.textSecondary)
                            }
                        }

                        Divider()

                        MarkdownBodyView(content: document.content, suppressInitialHeading: document.title)
                    }
                    .padding(.horizontal, 36)
                    .padding(.vertical, 30)
                    .frame(maxWidth: 820, alignment: .leading)
                    .frame(maxWidth: .infinity, alignment: .topLeading)
                }
                .background(SDColors.background)
            } else {
                SelectDocumentEmptyState {
                    viewModel.createNewDocument()
                }
            }
        }
    }
}

// MARK: - Compact detail (iPhone)

private struct CompactDocumentDetailView: View {
    let documentID: String?
    @Bindable var viewModel: DocumentationExplorerViewModel
    @State private var showsAttributesSheet = false

    var body: some View {
        Group {
            if viewModel.isLoadingDocument {
                ProgressView("Opening document…")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(SDColors.background)
            } else if let document = viewModel.selectedDocument {
                ScrollView {
                    VStack(alignment: .leading, spacing: 18) {
                        VStack(alignment: .leading, spacing: 8) {
                            Text(document.title)
                                .font(.largeTitle.weight(.bold))
                            if !document.summary.isEmpty {
                                Text(document.summary)
                                    .font(.title3)
                                    .foregroundStyle(SDColors.textSecondary)
                            }
                        }

                        Divider()

                        MarkdownBodyView(content: document.content, suppressInitialHeading: document.title)
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 24)
                }
                .background(SDColors.background)
                .navigationTitle(document.title)
                #if os(iOS)
                .navigationBarTitleDisplayMode(.inline)
                #endif
                .toolbar {
                    ToolbarItem(placement: .primaryAction) {
                        HStack(spacing: 16) {
                            Button {
                                showsAttributesSheet = true
                            } label: {
                                Image(systemName: "info.circle")
                            }
                            Button("Edit") {
                                viewModel.editSelectedDocument()
                            }
                        }
                    }
                }
                .sheet(isPresented: $showsAttributesSheet) {
                    DocumentationAttributesPanel(viewModel: viewModel) {
                        showsAttributesSheet = false
                    }
                    .presentationDetents([.medium, .large])
                    .presentationDragIndicator(.visible)
                }
            } else {
                SDEmptyStateView("Document not found", systemImage: "doc.text", message: "Go back and select a document.")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(SDColors.background)
            }
        }
    }
}

private struct FilterEmptyState: View {
    let onClear: () -> Void

    var body: some View {
        VStack(spacing: 12) {
            ContentUnavailableView(
                "No documents match these filters.",
                systemImage: "line.3.horizontal.decrease.circle",
                description: Text("Clear filters to return to the full documentation tree.")
            )
            Button("Clear filters", action: onClear)
                .buttonStyle(.bordered)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(.regularMaterial)
    }
}

private struct SelectDocumentEmptyState: View {
    let onNewDocument: () -> Void

    var body: some View {
        VStack(spacing: 12) {
            ContentUnavailableView(
                "Select a document",
                systemImage: "doc.text",
                description: Text("Choose a document from the tree or create a new one.")
            )
            SDPrimaryButton("New Document", systemImage: "plus", action: onNewDocument)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(.regularMaterial)
    }
}

private struct DocumentationAttributesPanel: View {
    @Bindable var viewModel: DocumentationExplorerViewModel
    let onCollapse: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack {
                Text("Attributes")
                    .font(.headline)
                Spacer()
                SDGlassIconButton(systemImage: "xmark.circle.fill", accessibilityLabel: "Close attributes", action: onCollapse)
            }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)

            Divider()

            if let document = viewModel.selectedDocument {
                ScrollView {
                    VStack(alignment: .leading, spacing: 20) {
                        VStack(alignment: .leading, spacing: 10) {
                            let effectiveStatus = viewModel.effectiveStatus(for: document)
                            SDStatusBadge(title: effectiveStatus.displayName, tint: statusTint(effectiveStatus))
                            Text(document.attributes.module)
                                .font(.title3.weight(.semibold))
                                .lineLimit(2)
                            Text(document.attributes.team)
                                .font(.callout)
                                .foregroundStyle(SDColors.textSecondary)
                            if let updateReason = viewModel.updateReason(for: document) {
                                Label(updateReason, systemImage: "clock.badge.exclamationmark")
                                    .font(.caption)
                                .foregroundStyle(SDColors.warning)
                            }
                        }

                        SDPrimaryButton("Edit Document", systemImage: "pencil") {
                            viewModel.editSelectedDocument()
                        }

                        SDMetadataPanel("Ownership") {
                            SDMetadataGrid(minimumColumnWidth: 120) {
                                SDMetadataItem(label: "Owner", value: document.attributes.owner, systemImage: "person.crop.circle")
                                SDMetadataItem(label: "Module", value: document.attributes.module, systemImage: "square.stack.3d.up")
                                SDMetadataItem(label: "Team", value: document.attributes.team, systemImage: "person.3")
                            }
                        }

                        SDMetadataPanel("Timeline") {
                            SDMetadataGrid(minimumColumnWidth: 120) {
                                SDMetadataItem(label: "Created", value: document.attributes.createdAt.formatted(.dateTime.month().day().year()), systemImage: "calendar.badge.plus")
                                SDMetadataItem(label: "Last updated", value: document.attributes.lastUpdated.formatted(.dateTime.month().day().year()), systemImage: "clock.arrow.circlepath")
                            }
                            if let updateReason = viewModel.updateReason(for: document) {
                                SDMetadataItem(label: "Update reason", value: updateReason, systemImage: "exclamationmark.triangle")
                            }
                        }

                        SDMetadataPanel("Tags") {
                            SDMetadataTagGroup(tags: document.attributes.tags)
                        }
                    }
                    .padding(.horizontal, 18)
                    .padding(.vertical, 18)
                }
            } else {
                SDEmptyStateView("No Selection", systemImage: "sidebar.right", message: "Document attributes will appear here after selecting a document.")
            }
        }
        .background(SDColors.sidebarBackground.opacity(0.88))
    }

    private func statusTint(_ status: DocumentationStatus) -> Color {
        switch status {
        case .draft: SDColors.textMuted
        case .published: SDColors.success
        case .updatesPending: SDColors.warning
        case .conflicted, .locked: SDColors.warning
        case .archived: SDColors.textMuted
        case .deletedSource: SDColors.danger
        case .outdated: SDColors.danger
        }
    }
}

#Preview {
    DocumentationExplorerView()
}
