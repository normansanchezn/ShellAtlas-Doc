import Foundation

public struct DocumentDraft: Identifiable, Sendable {
    public let id: String
    public var title: String
    public var content: String
    let originalContent: String
    public var lastAutoSaved: Date?

    public var isDirty: Bool { content != originalContent }

    public init(from document: KnowledgeDocument) {
        id              = document.id
        title           = document.title
        content         = document.content
        originalContent = document.content
    }
}
