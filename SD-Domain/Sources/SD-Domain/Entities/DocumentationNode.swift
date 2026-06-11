import Foundation

public struct DocumentationNode: Identifiable, Equatable, Sendable {
    public let id: String
    public let title: String
    public let type: DocumentationNodeType
    public let children: [DocumentationNode]
    public let documentId: String?

    public init(
        id: String,
        title: String,
        type: DocumentationNodeType,
        children: [DocumentationNode] = [],
        documentId: String? = nil
    ) {
        self.id = id
        self.title = title
        self.type = type
        self.children = children
        self.documentId = documentId
    }
}

public enum DocumentationNodeType: Equatable, Sendable {
    case folder
    case document
}
