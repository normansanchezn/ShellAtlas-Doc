import SwiftUI
import SD_DesignSystem
import SD_Domain
import DS_Core

struct DocumentsListView: View {
    @Environment(\.appServices) private var services
    @State private var viewModel = DocumentsListViewModel()

    var body: some View {
        Group {
            if viewModel.isLoading {
                ProgressView("Loading documents...")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if viewModel.filteredDocuments.isEmpty {
                emptyState
            } else {
                documentList
            }
        }
        .navigationTitle("Documents")
        .searchable(text: Bindable(viewModel).searchText, prompt: "Search documents…")
        .toolbar { filterToolbar }
        .onAppear {
            Task {
                guard let services else { return }
                try await viewModel.load(services: services)
            }
        }
    }

    private var documentList: some View {
        ScrollView {
            LazyVStack(spacing: 10) {
                ForEach(viewModel.filteredDocuments) { document in
                    NavigationLink(value: AppRoute.document(id: document.id)) {
                        SDDocumentCard(
                            document: document,
                            healthResult: viewModel.healthResults[document.id]
                        )
                        .contentShape(.rect)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding()
        }
        .navigationDestination(for: AppRoute.self) { route in
            if case .document(id: let id) = route {
                DocumentDetailView(documentID: id)
            }
        }
    }

    private var emptyState: some View {
        ContentUnavailableView(
            viewModel.searchText.isEmpty ? "No Documents" : "No Results",
            systemImage: "doc.text.magnifyingglass",
            description: Text(viewModel.searchText.isEmpty
                ? "No documents found in the knowledge base."
                : "No documents match '\(viewModel.searchText)'.")
        )
    }

    @ToolbarContentBuilder
    private var filterToolbar: some ToolbarContent {
        ToolbarItem(placement: .automatic) {
            Menu {
                Picker("Platform", selection: Bindable(viewModel).filterPlatform) {
                    Text("All Platforms").tag(nil as Platform?)
                    ForEach(Platform.allCases, id: \.self) { p in
                        Text(p.displayName).tag(p as Platform?)
                    }
                }
                Picker("Status", selection: Bindable(viewModel).filterStatus) {
                    Text("All Statuses").tag(nil as DocumentStatus?)
                    ForEach(DocumentStatus.allCases, id: \.self) { s in
                        Text(s.displayName).tag(s as DocumentStatus?)
                    }
                }
                Picker("Type", selection: Bindable(viewModel).filterType) {
                    Text("All Types").tag(nil as DocumentType?)
                    ForEach(DocumentType.allCases, id: \.self) { t in
                        Text(t.displayName).tag(t as DocumentType?)
                    }
                }
                Button("Reset Filters", role: .destructive, action: viewModel.resetFilters)
            } label: {
                Label("Filter", systemImage: "line.3.horizontal.decrease.circle")
            }
        }
    }
}

#Preview {
    NavigationStack {
        DocumentsListView()
    }
}
