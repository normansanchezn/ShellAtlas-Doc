import Foundation
import SD_Domain

public struct RemoteDocumentListResponse: Decodable, Sendable {
    public let documents: [RemoteDocumentationDocumentDTO]
}

public struct RemoteDocumentVersionsResponse: Decodable, Sendable {
    public let versions: [RemoteDocumentationVersionDTO]
}

public struct RemoteDocumentationDocumentDTO: Decodable, Sendable {
    public let id: String
    public let title: String
    public let summary: String?
    public let status: String?
    public let rawMarkdown: String?
    public let contentJson: DocumentationContentJSON?
    public let contentPlaintext: String?
    public let attributes: RemoteDocumentationAttributesDTO?
    public let createdAt: Date?
    public let updatedAt: Date?
}

public struct RemoteDocumentationAttributesDTO: Codable, Sendable {
    public let owner: String?
    public let module: String?
    public let team: String?
    public let status: String?
    public let tags: [String]?
    public let parentFolderId: String?
    public let platform: String?
    public let domain: String?
}

public struct RemoteDocumentationVersionDTO: Decodable, Sendable {
    public let id: String
    public let documentId: String
    public let versionNumber: Int
    public let title: String
    public let rawMarkdown: String
    public let contentJson: DocumentationContentJSON?
    public let contentPlaintext: String
    public let contentHash: String
    public let changeSummary: String?
    public let sourceVersion: String?
    public let createdAt: Date
}

public struct RemoteDocumentSaveRequest: Encodable, Sendable {
    public let title: String
    public let summary: String
    public let rawMarkdown: String
    public let status: String
    public let attributes: RemoteDocumentationAttributesDTO
    public let changeSummary: String?

    public init(document: DocumentationDocument, changeSummary: String? = nil) {
        self.title = document.title
        self.summary = document.summary
        self.rawMarkdown = document.content
        self.status = document.attributes.status.rawValue
        self.attributes = RemoteDocumentationAttributesDTO(
            owner: document.attributes.owner,
            module: document.attributes.module,
            team: document.attributes.team,
            status: document.attributes.status.rawValue,
            tags: document.attributes.tags,
            parentFolderId: document.attributes.parentFolderId,
            platform: nil,
            domain: nil
        )
        self.changeSummary = changeSummary
    }
}

public struct RemoteDraftSaveRequest: Encodable, Sendable {
    public let rawMarkdown: String
}
