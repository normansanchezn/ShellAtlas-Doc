import Foundation
import SD_Domain

public struct MockRepositorySignalRepository: RepositorySignalRepository {
    public init() {}
    public func getCommits() async throws -> [RepositoryCommit] {
        let dtos: [RepositoryCommitDTO] = try MockJSONLoader.load("commits")
        return dtos.map { RepositoryCommitMapper.toDomain($0) }
    }

    public func getWorkflowChanges() async throws -> [WorkflowChange] {
        let dtos: [WorkflowChangeDTO] = try MockJSONLoader.load("workflows")
        return dtos.map { WorkflowChangeMapper.toDomain($0) }
    }
}
