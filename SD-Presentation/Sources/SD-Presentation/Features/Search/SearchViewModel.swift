import Foundation
import SD_Domain

@MainActor
@Observable
final class SearchViewModel {
    var query = ""
    var results: [KnowledgeDocument] = []
    var isSearching = false
    var hasSearched = false

    func search(using useCase: SearchKnowledgeUseCase) async throws {
        guard !query.trimmingCharacters(in: .whitespaces).isEmpty else {
            results = []
            hasSearched = false
            return
        }
        isSearching = true
        do {
            results = try await useCase.execute(query: query)
            hasSearched = true
        } catch {
            results = []
        }
        isSearching = false
    }
}
