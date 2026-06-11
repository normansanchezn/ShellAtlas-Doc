import Foundation
import SD_Domain
import DS_Core

@MainActor
@Observable
final class DocumentsListViewModel {
    var documents: [KnowledgeDocument] = []
    var healthResults: [String: DocumentHealthResult] = [:]
    var searchText = ""
    var filterPlatform: Platform? = nil
    var filterStatus: DocumentStatus? = nil
    var filterType: DocumentType? = nil
    var filterPriority: AIReviewPriority? = nil
    var isLoading = false
    var errorMessage: String?

    var filteredDocuments: [KnowledgeDocument] {
        var result = documents
        if !searchText.isEmpty {
            result = result.filter { doc in
                doc.title.localizedStandardContains(searchText) ||
                doc.summary.localizedStandardContains(searchText) ||
                doc.tags.contains { $0.localizedStandardContains(searchText) }
            }
        }
        if let platform = filterPlatform {
            result = result.filter { $0.platform == platform }
        }
        if let status = filterStatus {
            result = result.filter { $0.status == status }
        }
        if let type = filterType {
            result = result.filter { $0.type == type }
        }
        if let priority = filterPriority {
            result = result.filter { $0.aiReviewPriority == priority }
        }
        return result.sorted { $0.lastUpdated > $1.lastUpdated }
    }

    func load(services: any AppServices) async throws {
        isLoading = true
        errorMessage = nil
        do {
            documents = try await services.getDocumentsUseCase.execute()
            for doc in documents {
                let health = try await services.evaluateDocumentHealthUseCase.execute(document: doc)
                healthResults[doc.id] = health
            }
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func resetFilters() {
        filterPlatform = nil
        filterStatus = nil
        filterType = nil
        filterPriority = nil
        searchText = ""
    }
}
