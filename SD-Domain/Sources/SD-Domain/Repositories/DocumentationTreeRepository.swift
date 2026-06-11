import Foundation

public protocol DocumentationTreeRepository: Sendable {
    func getDocumentationTree() async throws -> [DocumentationNode]
}
