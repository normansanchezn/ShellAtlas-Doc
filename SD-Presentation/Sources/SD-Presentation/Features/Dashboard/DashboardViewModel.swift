import Foundation
import SD_Domain

struct DashboardModuleMetric: Identifiable, Equatable {
    let id: String
    let module: String
    let count: Int
}

struct DashboardDocumentReviewMetric: Identifiable, Equatable {
    let id: String
    let title: String
    let module: String
    let lastReviewed: Date
}

@MainActor
@Observable
final class DashboardViewModel {
    var totalDocuments = 0
    var updatedDocuments = 0
    var outdatedDocuments = 0
    var pendingUpdateDocuments = 0
    var draftDocuments = 0
    var outdatedModules: [DashboardModuleMetric] = []
    var updatedModules: [DashboardModuleMetric] = []
    var mostReviewedDocuments: [DashboardDocumentReviewMetric] = []
    var isLoading = false
    var errorMessage: String?

    func load(services: any AppServices) async throws {
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }

        do {
            let documents = try await services.getDocumentationDocumentsUseCase.execute()
            totalDocuments = documents.count

            let classified = documents.map { document in
                (document, DocumentationReviewPolicy.effectiveStatus(for: document))
            }

            updatedDocuments = classified.filter { _, status in status == .published }.count
            outdatedDocuments = classified.filter { _, status in
                status == .outdated || status == .updatesPending
            }.count
            pendingUpdateDocuments = classified.filter { _, status in status == .updatesPending }.count
            draftDocuments = classified.filter { _, status in status == .draft }.count

            outdatedModules = moduleMetrics(
                from: classified
                    .filter { _, status in status == .outdated || status == .updatesPending }
                    .map(\.0),
                limit: 6
            )
            updatedModules = moduleMetrics(
                from: classified
                    .filter { _, status in status == .published }
                    .map(\.0),
                limit: 6
            )
            mostReviewedDocuments = documents
                .sorted { $0.attributes.lastUpdated > $1.attributes.lastUpdated }
                .prefix(8)
                .map {
                    DashboardDocumentReviewMetric(
                        id: $0.id,
                        title: $0.title,
                        module: $0.attributes.module,
                        lastReviewed: $0.attributes.lastUpdated
                    )
                }
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func moduleMetrics(from documents: [DocumentationDocument], limit: Int) -> [DashboardModuleMetric] {
        Dictionary(grouping: documents, by: { moduleName($0) })
            .map { module, documents in
                DashboardModuleMetric(id: module, module: module, count: documents.count)
            }
            .sorted {
                if $0.count == $1.count {
                    return $0.module.localizedStandardCompare($1.module) == .orderedAscending
                }
                return $0.count > $1.count
            }
            .prefix(limit)
            .map { $0 }
    }

    private func moduleName(_ document: DocumentationDocument) -> String {
        let trimmed = document.attributes.module.trimmingCharacters(in: .whitespacesAndNewlines)
        return trimmed.isEmpty ? "Unassigned" : trimmed
    }
}
