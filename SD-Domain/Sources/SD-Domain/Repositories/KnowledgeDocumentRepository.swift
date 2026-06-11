import Foundation

public protocol KnowledgeDocumentRepository: Sendable {
    func getDocuments() async throws -> [KnowledgeDocument]
    func getDocument(id: String) async throws -> KnowledgeDocument?
}
