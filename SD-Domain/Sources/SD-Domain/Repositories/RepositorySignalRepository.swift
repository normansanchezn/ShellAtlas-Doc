import Foundation

public protocol RepositorySignalRepository: Sendable {
    func getCommits() async throws -> [RepositoryCommit]
    func getWorkflowChanges() async throws -> [WorkflowChange]
}
