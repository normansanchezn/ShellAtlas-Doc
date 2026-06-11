import Foundation

struct RepositoryCommitDTO: Codable, Sendable {
    let id: String
    let sha: String
    let message: String
    let author: String
    let date: Date
    let changedFiles: [String]
    let repository: String
    let branch: String
    let relatedDocumentIDs: [String]
}
