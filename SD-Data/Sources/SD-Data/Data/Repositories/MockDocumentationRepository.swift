import Foundation
import SD_Domain

public struct MockDocumentationRepository: DocumentationRepository {
    private let store: DocumentationMockStore

    public init() {
        self.store = DocumentationMockData.sharedStore
    }

    init(store: DocumentationMockStore) {
        self.store = store
    }

    public func getDocuments() async throws -> [DocumentationDocument] {
        await store.getDocuments()
    }

    public func getDocument(id: String) async throws -> DocumentationDocument {
        try await store.getDocument(id: id)
    }

    public func searchDocumentsByTitle(_ query: String) async throws -> [DocumentationDocument] {
        await store.searchDocumentsByTitle(query)
    }

    public func createDocument(_ document: DocumentationDocument) async throws {
        try await store.createDocument(document)
    }

    public func updateDocument(_ document: DocumentationDocument) async throws {
        try await store.updateDocument(document)
    }

    public func deleteDocument(id: String) async throws {
        try await store.deleteDocument(id: id)
    }

    public func saveDraft(documentId: String, rawMarkdown: String) async throws -> DocumentationDraftReceipt {
        _ = try await store.getDocument(id: documentId)
        return DocumentationDraftReceipt(
            documentId: documentId,
            draftSaved: true,
            contentHash: "mock:\(rawMarkdown.hashValue)",
            updatedAt: Date()
        )
    }

    public func getVersions(documentId: String) async throws -> [DocumentationVersion] {
        try await store.versions(for: documentId)
    }

    public func restoreVersion(documentId: String, versionId: String) async throws -> DocumentationDocument {
        try await store.getDocument(id: documentId)
    }
}
