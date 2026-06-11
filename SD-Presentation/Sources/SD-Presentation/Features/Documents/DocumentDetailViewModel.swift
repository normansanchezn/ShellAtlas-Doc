import Foundation
import SD_Domain
import DS_Core

@MainActor
@Observable
final class DocumentDetailViewModel {
    var document: KnowledgeDocument?
    var healthResult: DocumentHealthResult?
    var relatedSignals: [KnowledgeSignal] = []
    var isLoading = false
    var errorMessage: String?

    func load(documentID: String, services: any AppServices) async throws {
        isLoading = true
        errorMessage = nil
        do {
            let doc = try await services.getDocumentDetailUseCase.execute(id: documentID)
            document = doc
             let health = try await services.evaluateDocumentHealthUseCase.execute(document: doc)
             let signals = try await services.getRelatedSignalsUseCase.execute(for: doc)
            let (h, s) = try await (health, signals)
            healthResult = h
            relatedSignals = s
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }
}
