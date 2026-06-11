import Foundation
import SD_Domain

public protocol DocumentRemoteDataSource: Sendable {
    func getDocuments() async throws -> [RemoteDocumentationDocumentDTO]
    func getDocument(id: String) async throws -> RemoteDocumentationDocumentDTO
    func searchDocuments(query: String) async throws -> [RemoteDocumentationDocumentDTO]
    func createDocument(_ request: RemoteDocumentSaveRequest) async throws -> RemoteDocumentationDocumentDTO
    func publishDocument(id: String, request: RemoteDocumentSaveRequest) async throws -> RemoteDocumentationDocumentDTO
    func saveDraft(documentId: String, rawMarkdown: String) async throws -> DocumentationDraftReceipt
    func getVersions(documentId: String) async throws -> [RemoteDocumentationVersionDTO]
    func restoreVersion(documentId: String, versionId: String) async throws -> RemoteDocumentationDocumentDTO
    func deleteDocument(id: String) async throws
}

public struct APIDocumentRemoteDataSource: DocumentRemoteDataSource {
    private let client: any ShellDocAPIClientProtocol

    public init(client: any ShellDocAPIClientProtocol) {
        self.client = client
    }

    public func getDocuments() async throws -> [RemoteDocumentationDocumentDTO] {
        try await client.request("/v1/documents", method: "GET", queryItems: [], body: Optional<String>.none, responseType: RemoteDocumentListResponse.self).documents
    }

    public func getDocument(id: String) async throws -> RemoteDocumentationDocumentDTO {
        try await client.request("/v1/documents/\(id)", method: "GET", queryItems: [], body: Optional<String>.none, responseType: RemoteDocumentationDocumentDTO.self)
    }

    public func searchDocuments(query: String) async throws -> [RemoteDocumentationDocumentDTO] {
        try await client.request(
            "/v1/search",
            method: "GET",
            queryItems: [URLQueryItem(name: "q", value: query)],
            body: Optional<String>.none,
            responseType: RemoteDocumentListResponse.self
        ).documents
    }

    public func createDocument(_ request: RemoteDocumentSaveRequest) async throws -> RemoteDocumentationDocumentDTO {
        try await client.request("/v1/documents", method: "POST", queryItems: [], body: request, responseType: RemoteDocumentationDocumentDTO.self)
    }

    public func publishDocument(id: String, request: RemoteDocumentSaveRequest) async throws -> RemoteDocumentationDocumentDTO {
        try await client.request("/v1/documents/\(id)/publish", method: "POST", queryItems: [], body: request, responseType: RemoteDocumentationDocumentDTO.self)
    }

    public func saveDraft(documentId: String, rawMarkdown: String) async throws -> DocumentationDraftReceipt {
        try await client.request(
            "/v1/documents/\(documentId)/draft",
            method: "POST",
            queryItems: [],
            body: RemoteDraftSaveRequest(rawMarkdown: rawMarkdown),
            responseType: DocumentationDraftReceipt.self
        )
    }

    public func getVersions(documentId: String) async throws -> [RemoteDocumentationVersionDTO] {
        try await client.request(
            "/v1/documents/\(documentId)/versions",
            method: "GET",
            queryItems: [],
            body: Optional<String>.none,
            responseType: RemoteDocumentVersionsResponse.self
        ).versions
    }

    public func restoreVersion(documentId: String, versionId: String) async throws -> RemoteDocumentationDocumentDTO {
        try await client.request(
            "/v1/documents/\(documentId)/restore/\(versionId)",
            method: "POST",
            queryItems: [],
            body: Optional<String>.none,
            responseType: RemoteDocumentationDocumentDTO.self
        )
    }

    public func deleteDocument(id: String) async throws {
        try await client.requestNoContent("/v1/documents/\(id)", method: "DELETE", body: Optional<String>.none)
    }
}
