import Foundation
import SD_Domain

private actor DocumentationDocumentCache {
    var documents: [DocumentationDocument]? = nil

    func get() -> [DocumentationDocument]? { documents }
    func set(_ docs: [DocumentationDocument]) { documents = docs }
    func invalidate() { documents = nil }
}

public final class CachingDocumentationRepository: DocumentationRepository, @unchecked Sendable {
    private let wrapped: any DocumentationRepository
    private let cache = DocumentationDocumentCache()

    public init(wrapped: any DocumentationRepository) {
        self.wrapped = wrapped
    }

    public func getDocuments() async throws -> [DocumentationDocument] {
        if let cached = await cache.get() {
            return cached
        }
        let docs = try await wrapped.getDocuments()
        await cache.set(docs)
        return docs
    }

    public func getDocument(id: String) async throws -> DocumentationDocument {
        if let cached = await cache.get(), let doc = cached.first(where: { $0.id == id }) {
            return doc
        }
        return try await wrapped.getDocument(id: id)
    }

    public func searchDocumentsByTitle(_ query: String) async throws -> [DocumentationDocument] {
        try await wrapped.searchDocumentsByTitle(query)
    }

    public func createDocument(_ document: DocumentationDocument) async throws {
        try await wrapped.createDocument(document)
        await cache.invalidate()
    }

    public func updateDocument(_ document: DocumentationDocument) async throws {
        try await wrapped.updateDocument(document)
        await cache.invalidate()
    }

    public func deleteDocument(id: String) async throws {
        try await wrapped.deleteDocument(id: id)
        await cache.invalidate()
    }

    public func saveDraft(documentId: String, rawMarkdown: String) async throws -> DocumentationDraftReceipt {
        try await wrapped.saveDraft(documentId: documentId, rawMarkdown: rawMarkdown)
    }

    public func getVersions(documentId: String) async throws -> [DocumentationVersion] {
        try await wrapped.getVersions(documentId: documentId)
    }

    public func restoreVersion(documentId: String, versionId: String) async throws -> DocumentationDocument {
        let doc = try await wrapped.restoreVersion(documentId: documentId, versionId: versionId)
        await cache.invalidate()
        return doc
    }
}
