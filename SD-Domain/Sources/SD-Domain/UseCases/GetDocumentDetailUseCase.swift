import Foundation

public struct GetDocumentDetailUseCase: Sendable {
    let repository: any KnowledgeDocumentRepository

    public init(repository: any KnowledgeDocumentRepository) {
        self.repository = repository
    }

    public func execute(id: String) async throws -> KnowledgeDocument {
        guard let document = try await repository.getDocument(id: id) else {
            throw DomainError.documentNotFound(id)
        }
        return document
    }
}
