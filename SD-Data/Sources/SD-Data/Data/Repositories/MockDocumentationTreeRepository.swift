import Foundation
import SD_Domain

public struct MockDocumentationTreeRepository: DocumentationTreeRepository {
    private let store: DocumentationMockStore

    public init() {
        self.store = DocumentationMockData.sharedStore
    }

    init(store: DocumentationMockStore) {
        self.store = store
    }

    public func getDocumentationTree() async throws -> [DocumentationNode] {
        let documents = await store.getDocuments()
        return DocumentationMockData.documentationTree(for: documents)
    }
}
