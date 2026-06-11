import Foundation

public struct DocumentationDocument: Identifiable, Equatable, Sendable, Codable {
    public let id: String
    public var title: String
    public var summary: String
    public var content: String
    public var contentJSON: DocumentationContentJSON?
    public var attributes: DocumentationAttributes

    public init(
        id: String,
        title: String,
        summary: String,
        content: String,
        contentJSON: DocumentationContentJSON? = nil,
        attributes: DocumentationAttributes
    ) {
        self.id = id
        self.title = title
        self.summary = summary
        self.content = content
        self.contentJSON = contentJSON
        self.attributes = attributes
    }
}

public struct DocumentationContentJSON: Equatable, Sendable, Codable {
    public var schemaVersion: Int
    public var blocks: [DocumentationContentBlock]

    public init(schemaVersion: Int = 1, blocks: [DocumentationContentBlock]) {
        self.schemaVersion = schemaVersion
        self.blocks = blocks
    }
}

public struct DocumentationContentBlock: Equatable, Sendable, Codable {
    public var type: String
    public var level: Int?
    public var text: String?
    public var style: String?
    public var items: [String]?
    public var language: String?
    public var code: String?

    public init(
        type: String,
        level: Int? = nil,
        text: String? = nil,
        style: String? = nil,
        items: [String]? = nil,
        language: String? = nil,
        code: String? = nil
    ) {
        self.type = type
        self.level = level
        self.text = text
        self.style = style
        self.items = items
        self.language = language
        self.code = code
    }
}

public struct DocumentationAttributes: Equatable, Sendable, Codable {
    public var owner: String
    public var module: String
    public var team: String
    public var status: DocumentationStatus
    public var tags: [String]
    public var lastUpdated: Date
    public var createdAt: Date
    public var parentFolderId: String?

    public init(
        owner: String,
        module: String,
        team: String,
        status: DocumentationStatus,
        tags: [String],
        lastUpdated: Date,
        createdAt: Date,
        parentFolderId: String? = nil
    ) {
        self.owner = owner
        self.module = module
        self.team = team
        self.status = status
        self.tags = tags
        self.lastUpdated = lastUpdated
        self.createdAt = createdAt
        self.parentFolderId = parentFolderId
    }
}

public enum DocumentationStatus: String, Equatable, CaseIterable, Sendable, Codable {
    case draft
    case published
    case updatesPending = "updates_pending"
    case conflicted
    case archived
    case deletedSource = "deleted_source"
    case locked
    case outdated

    public var displayName: String {
        switch self {
        case .draft: "Draft"
        case .published: "Published"
        case .updatesPending: "Updates Pending"
        case .conflicted: "Conflicted"
        case .archived: "Archived"
        case .deletedSource: "Deleted Source"
        case .locked: "Locked"
        case .outdated: "Outdated"
        }
    }
}

public struct DocumentationVersion: Identifiable, Equatable, Sendable, Codable {
    public let id: String
    public var documentId: String
    public var versionNumber: Int
    public var title: String
    public var rawMarkdown: String
    public var contentJSON: DocumentationContentJSON?
    public var contentPlaintext: String
    public var contentHash: String
    public var changeSummary: String?
    public var sourceVersion: String?
    public var createdAt: Date

    public init(
        id: String,
        documentId: String,
        versionNumber: Int,
        title: String,
        rawMarkdown: String,
        contentJSON: DocumentationContentJSON? = nil,
        contentPlaintext: String,
        contentHash: String,
        changeSummary: String? = nil,
        sourceVersion: String? = nil,
        createdAt: Date
    ) {
        self.id = id
        self.documentId = documentId
        self.versionNumber = versionNumber
        self.title = title
        self.rawMarkdown = rawMarkdown
        self.contentJSON = contentJSON
        self.contentPlaintext = contentPlaintext
        self.contentHash = contentHash
        self.changeSummary = changeSummary
        self.sourceVersion = sourceVersion
        self.createdAt = createdAt
    }
}

public struct DocumentationDraftReceipt: Equatable, Sendable, Codable {
    public let documentId: String
    public var draftSaved: Bool
    public var contentHash: String
    public var updatedAt: Date

    public init(documentId: String, draftSaved: Bool, contentHash: String, updatedAt: Date) {
        self.documentId = documentId
        self.draftSaved = draftSaved
        self.contentHash = contentHash
        self.updatedAt = updatedAt
    }
}
