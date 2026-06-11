import Foundation

public protocol GetDocumentationTreeUseCaseProtocol: Sendable {
    func execute() async throws -> [DocumentationNode]
}

public protocol GetDocumentationDocumentDetailUseCaseProtocol: Sendable {
    func execute(id: String) async throws -> DocumentationDocument
}

public protocol GetDocumentationDocumentsUseCaseProtocol: Sendable {
    func execute() async throws -> [DocumentationDocument]
}

public protocol GetDocumentDetailUseCaseProtocol: Sendable {
    func execute(id: String) async throws -> DocumentationDocument
}

public protocol SearchDocumentsByTitleUseCaseProtocol: Sendable {
    func execute(query: String) async throws -> [DocumentationDocument]
}

public protocol CreateDocumentUseCaseProtocol: Sendable {
    func execute(document: DocumentationDocument) async throws
}

public protocol UpdateDocumentUseCaseProtocol: Sendable {
    func execute(document: DocumentationDocument) async throws
}

public protocol DeleteDocumentUseCaseProtocol: Sendable {
    func execute(id: String) async throws
}

public protocol SaveDocumentationDraftUseCaseProtocol: Sendable {
    func execute(documentId: String, rawMarkdown: String) async throws -> DocumentationDraftReceipt
}

public protocol GetDocumentationVersionsUseCaseProtocol: Sendable {
    func execute(documentId: String) async throws -> [DocumentationVersion]
}

public protocol RestoreDocumentationVersionUseCaseProtocol: Sendable {
    func execute(documentId: String, versionId: String) async throws -> DocumentationDocument
}

public final class GetDocumentationTreeUseCase: GetDocumentationTreeUseCaseProtocol, @unchecked Sendable {
    private let repository: any DocumentationTreeRepository

    public init(repository: any DocumentationTreeRepository) {
        self.repository = repository
    }

    public func execute() async throws -> [DocumentationNode] {
        try await repository.getDocumentationTree()
    }
}

public final class GetDocumentationDocumentsUseCase: GetDocumentationDocumentsUseCaseProtocol, @unchecked Sendable {
    private let repository: any DocumentationRepository

    public init(repository: any DocumentationRepository) {
        self.repository = repository
    }

    public func execute() async throws -> [DocumentationDocument] {
        try await repository.getDocuments()
    }
}

public final class GetDocumentationDocumentDetailUseCase: GetDocumentationDocumentDetailUseCaseProtocol, GetDocumentDetailUseCaseProtocol, @unchecked Sendable {
    private let repository: any DocumentationRepository

    public init(repository: any DocumentationRepository) {
        self.repository = repository
    }

    public func execute(id: String) async throws -> DocumentationDocument {
        try await repository.getDocument(id: id)
    }
}

public final class SearchDocumentsByTitleUseCase: SearchDocumentsByTitleUseCaseProtocol, @unchecked Sendable {
    private let repository: any DocumentationRepository

    public init(repository: any DocumentationRepository) {
        self.repository = repository
    }

    public func execute(query: String) async throws -> [DocumentationDocument] {
        let trimmedQuery = query.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedQuery.isEmpty else { return [] }
        return try await repository.searchDocumentsByTitle(trimmedQuery)
    }
}

public final class CreateDocumentUseCase: CreateDocumentUseCaseProtocol, @unchecked Sendable {
    private let repository: any DocumentationRepository

    public init(repository: any DocumentationRepository) {
        self.repository = repository
    }

    public func execute(document: DocumentationDocument) async throws {
        try await repository.createDocument(document)
    }
}

public final class UpdateDocumentUseCase: UpdateDocumentUseCaseProtocol, @unchecked Sendable {
    private let repository: any DocumentationRepository

    public init(repository: any DocumentationRepository) {
        self.repository = repository
    }

    public func execute(document: DocumentationDocument) async throws {
        try await repository.updateDocument(document)
    }
}

public final class DeleteDocumentUseCase: DeleteDocumentUseCaseProtocol, @unchecked Sendable {
    private let repository: any DocumentationRepository

    public init(repository: any DocumentationRepository) {
        self.repository = repository
    }

    public func execute(id: String) async throws {
        try await repository.deleteDocument(id: id)
    }
}

public final class SaveDocumentationDraftUseCase: SaveDocumentationDraftUseCaseProtocol, @unchecked Sendable {
    private let repository: any DocumentationRepository

    public init(repository: any DocumentationRepository) {
        self.repository = repository
    }

    public func execute(documentId: String, rawMarkdown: String) async throws -> DocumentationDraftReceipt {
        try await repository.saveDraft(documentId: documentId, rawMarkdown: rawMarkdown)
    }
}

public final class GetDocumentationVersionsUseCase: GetDocumentationVersionsUseCaseProtocol, @unchecked Sendable {
    private let repository: any DocumentationRepository

    public init(repository: any DocumentationRepository) {
        self.repository = repository
    }

    public func execute(documentId: String) async throws -> [DocumentationVersion] {
        try await repository.getVersions(documentId: documentId)
    }
}

public final class RestoreDocumentationVersionUseCase: RestoreDocumentationVersionUseCaseProtocol, @unchecked Sendable {
    private let repository: any DocumentationRepository

    public init(repository: any DocumentationRepository) {
        self.repository = repository
    }

    public func execute(documentId: String, versionId: String) async throws -> DocumentationDocument {
        try await repository.restoreVersion(documentId: documentId, versionId: versionId)
    }
}
