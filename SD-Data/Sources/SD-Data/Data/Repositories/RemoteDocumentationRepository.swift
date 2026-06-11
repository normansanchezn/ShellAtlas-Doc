import Foundation
import SD_Domain

public struct RemoteDocumentationRepository: DocumentationRepository {
    private let remoteDataSource: any DocumentRemoteDataSource

    public init(remoteDataSource: any DocumentRemoteDataSource) {
        self.remoteDataSource = remoteDataSource
    }

    public func getDocuments() async throws -> [DocumentationDocument] {
        try await remoteDataSource.getDocuments().map(mapDocument)
    }

    public func getDocument(id: String) async throws -> DocumentationDocument {
        mapDocument(try await remoteDataSource.getDocument(id: id))
    }

    public func searchDocumentsByTitle(_ query: String) async throws -> [DocumentationDocument] {
        try await remoteDataSource.searchDocuments(query: query).map(mapDocument)
    }

    public func createDocument(_ document: DocumentationDocument) async throws {
        _ = try await remoteDataSource.createDocument(RemoteDocumentSaveRequest(document: document, changeSummary: "Created from ShellDoc"))
    }

    public func updateDocument(_ document: DocumentationDocument) async throws {
        _ = try await remoteDataSource.publishDocument(id: document.id, request: RemoteDocumentSaveRequest(document: document, changeSummary: "Published from ShellDoc"))
    }

    public func deleteDocument(id: String) async throws {
        try await remoteDataSource.deleteDocument(id: id)
    }

    public func saveDraft(documentId: String, rawMarkdown: String) async throws -> DocumentationDraftReceipt {
        try await remoteDataSource.saveDraft(documentId: documentId, rawMarkdown: rawMarkdown)
    }

    public func getVersions(documentId: String) async throws -> [DocumentationVersion] {
        try await remoteDataSource.getVersions(documentId: documentId).map(mapVersion)
    }

    public func restoreVersion(documentId: String, versionId: String) async throws -> DocumentationDocument {
        mapDocument(try await remoteDataSource.restoreVersion(documentId: documentId, versionId: versionId))
    }

    private func mapDocument(_ dto: RemoteDocumentationDocumentDTO) -> DocumentationDocument {
        let now = Date()
        let status = DocumentationStatus(rawValue: dto.attributes?.status ?? dto.status ?? "") ?? .draft
        let attributes = DocumentationAttributes(
            owner: dto.attributes?.owner ?? "Unassigned",
            module: dto.attributes?.module ?? dto.attributes?.domain ?? "Uncategorized",
            team: dto.attributes?.team ?? "Unassigned",
            status: status,
            tags: dto.attributes?.tags ?? [],
            lastUpdated: dto.updatedAt ?? now,
            createdAt: dto.createdAt ?? now,
            parentFolderId: dto.attributes?.parentFolderId
        )
        return DocumentationDocument(
            id: dto.id,
            title: dto.title,
            summary: dto.summary ?? "",
            content: dto.rawMarkdown ?? dto.contentPlaintext ?? "",
            contentJSON: dto.contentJson,
            attributes: attributes
        )
    }

    private func mapVersion(_ dto: RemoteDocumentationVersionDTO) -> DocumentationVersion {
        DocumentationVersion(
            id: dto.id,
            documentId: dto.documentId,
            versionNumber: dto.versionNumber,
            title: dto.title,
            rawMarkdown: dto.rawMarkdown,
            contentJSON: dto.contentJson,
            contentPlaintext: dto.contentPlaintext,
            contentHash: dto.contentHash,
            changeSummary: dto.changeSummary,
            sourceVersion: dto.sourceVersion,
            createdAt: dto.createdAt
        )
    }
}
