import Foundation
import Observation
import SD_Domain
import DS_Core

@MainActor
@Observable
public final class OutdatedReviewViewModel {
    var documents: [DocumentationDocument] = []
    var selectedDocumentID: String?
    var selectedDocument: DocumentationDocument?
    var isLoading = false
    var errorMessage: String?

    private var getDocumentsUseCase: (any GetDocumentationDocumentsUseCaseProtocol)?
    private var getDocumentUseCase: (any GetDocumentationDocumentDetailUseCaseProtocol)?

    func configure(services: any AppServices) {
        getDocumentsUseCase = services.getDocumentationDocumentsUseCase
        getDocumentUseCase = services.getDocumentationDocumentDetailUseCase
    }

    func loadDocuments(selectFirstIfNeeded: Bool = true) async {
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }

        do {
            let loadedDocuments = try await getDocumentsUseCase?.execute() ?? []
            documents = loadedDocuments
                .filter { DocumentationReviewPolicy.updatesPending($0) }
                .sorted(by: sortDocuments)

            if let selectedDocumentID, documents.contains(where: { $0.id == selectedDocumentID }) {
                await selectDocument(id: selectedDocumentID)
            } else if selectFirstIfNeeded, let firstDocument = documents.first {
                await selectDocument(id: firstDocument.id)
            } else {
                selectedDocumentID = nil
                selectedDocument = nil
            }
        } catch {
            documents = []
            selectedDocument = nil
            selectedDocumentID = nil
            errorMessage = error.localizedDescription
        }
    }

    func selectDocument(id: String) async {
        selectedDocumentID = id
        errorMessage = nil

        do {
            selectedDocument = try await getDocumentUseCase?.execute(id: id)
        } catch {
            selectedDocument = nil
            errorMessage = error.localizedDescription
        }
    }

    func refreshAfterSave(documentID: String) async {
        selectedDocumentID = documentID
        await loadDocuments(selectFirstIfNeeded: false)
    }

    func effectiveStatus(for document: DocumentationDocument) -> DocumentationStatus {
        DocumentationReviewPolicy.effectiveStatus(for: document)
    }

    func updateReason(for document: DocumentationDocument) -> String {
        DocumentationReviewPolicy.updateReason(for: document) ?? "Update required"
    }

    private func sortDocuments(_ lhs: DocumentationDocument, _ rhs: DocumentationDocument) -> Bool {
        let lhsRank = statusSortRank(effectiveStatus(for: lhs))
        let rhsRank = statusSortRank(effectiveStatus(for: rhs))

        if lhsRank == rhsRank {
            return lhs.attributes.lastUpdated < rhs.attributes.lastUpdated
        }
        return lhsRank < rhsRank
    }

    private func statusSortRank(_ status: DocumentationStatus) -> Int {
        switch status {
        case .updatesPending: 0
        case .outdated: 1
        case .conflicted: 1
        case .deletedSource: 1
        case .locked: 2
        case .draft: 2
        case .published: 3
        case .archived: 4
        }
    }
}
