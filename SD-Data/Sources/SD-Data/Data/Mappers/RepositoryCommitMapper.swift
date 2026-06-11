import Foundation
import SD_Domain

enum RepositoryCommitMapper {
    static func toDomain(_ dto: RepositoryCommitDTO) -> RepositoryCommit {
        RepositoryCommit(
            id: dto.id,
            sha: dto.sha,
            message: dto.message,
            author: dto.author,
            date: dto.date,
            changedFiles: dto.changedFiles,
            repository: dto.repository,
            branch: dto.branch,
            relatedDocumentIDs: dto.relatedDocumentIDs
        )
    }
}
