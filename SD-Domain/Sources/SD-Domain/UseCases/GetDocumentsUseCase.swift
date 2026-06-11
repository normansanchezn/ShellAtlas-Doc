import Foundation

public struct GetDocumentsUseCase: Sendable {
    let repository: any KnowledgeDocumentRepository

    public init(repository: any KnowledgeDocumentRepository) {
        self.repository = repository
    }

    public func execute() async throws -> [KnowledgeDocument] {
        try await repository.getDocuments()
    }
}
