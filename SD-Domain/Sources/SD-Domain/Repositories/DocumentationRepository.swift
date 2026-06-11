import Foundation

public protocol DocumentationRepository: Sendable {
    func getDocuments() async throws -> [DocumentationDocument]
    func getDocument(id: String) async throws -> DocumentationDocument
    func searchDocumentsByTitle(_ query: String) async throws -> [DocumentationDocument]
    func createDocument(_ document: DocumentationDocument) async throws
    func updateDocument(_ document: DocumentationDocument) async throws
    func deleteDocument(id: String) async throws
    func saveDraft(documentId: String, rawMarkdown: String) async throws -> DocumentationDraftReceipt
    func getVersions(documentId: String) async throws -> [DocumentationVersion]
    func restoreVersion(documentId: String, versionId: String) async throws -> DocumentationDocument
}
