import SwiftUI
import SD_Domain
import DS_Core
import SD_DesignSystem

struct SearchView: View {
    @Environment(\.appServices) private var services
    @State private var viewModel = SearchViewModel()

    var body: some View {
        ScrollView {
            searchContent
                .padding()
        }
        .navigationTitle("Search")
        .searchable(text: Bindable(viewModel).query, prompt: "Search knowledge base…")
        .navigationDestination(for: AppRoute.self) { route in
            if case .document(id: let id) = route {
                DocumentDetailView(documentID: id)
            }
        }
        .onAppear {
            Task {
                guard let services else { return }
                try? await Task.sleep(for: .milliseconds(300))
                try await viewModel.search(using: services.searchKnowledgeUseCase)
            }
        }
    }

    private var searchContent: some View {
        LazyVStack(alignment: .leading, spacing: 12) {
            if !viewModel.query.isEmpty {
                aliasHints
            }
            searchStateContent
        }
    }

    @ViewBuilder
    private var searchStateContent: some View {
        if viewModel.isSearching {
            ProgressView()
                .frame(maxWidth: .infinity)
                .padding(.top, 20)
        } else if viewModel.hasSearched && viewModel.results.isEmpty {
            ContentUnavailableView.search(text: viewModel.query)
        } else if !viewModel.results.isEmpty {
            searchResults
        } else {
            searchSuggestions
        }
    }

    private var searchResults: some View {
        LazyVStack(alignment: .leading, spacing: 12) {
            ForEach(viewModel.results) { document in
                searchResultRow(for: document)
            }
        }
    }

    private func searchResultRow(for document: KnowledgeDocument) -> some View {
        NavigationLink(value: AppRoute.document(id: document.id)) {
            SDDocumentCard(document: document)
                .contentShape(.rect)
        }
        .buttonStyle(.plain)
    }

    private var aliasHints: some View {
        Group {
            if viewModel.query.localizedStandardContains("release") ||
               viewModel.query.localizedStandardContains("build") {
                SearchHint(text: "Tip: \"release build\" finds EoSB1, GitHub Actions, QA handoff docs")
            } else if viewModel.query.localizedStandardContains("local") {
                SearchHint(text: "Tip: \"localization\" expands to Lokalise, strings.xml, translations")
            } else if viewModel.query.localizedStandardContains("secret") ||
                      viewModel.query.localizedStandardContains("key") {
                SearchHint(text: "Tip: \"secrets\" finds Azure Secrets, Keychain, credentials docs")
            }
        }
    }

    private var searchSuggestions: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Try searching for")
                .font(.headline)
                .padding(.bottom, 4)
            let suggestions: [String] = [
                "release build", "pilot branch", "localization",
                "secrets", "authentication", "deep link", "rewards"
            ]
            ForEach(suggestions, id: \.self) { suggestion in
                Button(action: { viewModel.query = suggestion }) {
                    Label(suggestion, systemImage: "magnifyingglass")
                        .font(.body)
                        .foregroundStyle(.primary)
                }
            }
        }
        .padding(.top, 16)
    }
}

private struct SearchHint: View {
    let text: String

    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: "lightbulb.fill")
                .foregroundStyle(.yellow)
            Text(text)
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding(8)
        .background(.yellow.opacity(0.1), in: .rect(cornerRadius: 8))
    }
}

#Preview {
    NavigationStack {
        SearchView()
    }
}
