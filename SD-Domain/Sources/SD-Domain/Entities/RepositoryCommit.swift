import Foundation

public struct RepositoryCommit: Identifiable, Sendable {
    public let id: String
    public let sha: String
    public let message: String
    public let author: String
    public let date: Date
    public let changedFiles: [String]
    public let repository: String
    public let branch: String
    public let relatedDocumentIDs: [String]
    
    public init(id: String, sha: String, message: String, author: String, date: Date, changedFiles: [String], repository: String, branch: String, relatedDocumentIDs: [String]) {
        self.id = id
        self.sha = sha
        self.message = message
        self.author = author
        self.date = date
        self.changedFiles = changedFiles
        self.repository = repository
        self.branch = branch
        self.relatedDocumentIDs = relatedDocumentIDs
    }
}
