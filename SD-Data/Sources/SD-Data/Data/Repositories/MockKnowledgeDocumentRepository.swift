import Foundation
import SD_Domain

public struct MockKnowledgeDocumentRepository: KnowledgeDocumentRepository {
    
    public init() {
        
    }
    public func getDocuments() async throws -> [KnowledgeDocument] {
        let dtos: [KnowledgeDocumentDTO] = try MockJSONLoader.load("documents")
        return dtos.map { KnowledgeDocumentMapper.toDomain($0) }
    }

    public func getDocument(id: String) async throws -> KnowledgeDocument? {
        let documents = try await getDocuments()
        return documents.first { $0.id == id }
    }
}
