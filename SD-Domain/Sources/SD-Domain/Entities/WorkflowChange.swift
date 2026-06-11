import Foundation

public struct WorkflowChange: Identifiable, Sendable {
    public let id: String
    public let workflowName: String
    public let changedAt: Date
    public let description: String
    public let changedFiles: [String]
    public let relatedDocumentIDs: [String]
    
    public init(id: String, workflowName: String, changedAt: Date, description: String, changedFiles: [String], relatedDocumentIDs: [String]) {
        self.id = id
        self.workflowName = workflowName
        self.changedAt = changedAt
        self.description = description
        self.changedFiles = changedFiles
        self.relatedDocumentIDs = relatedDocumentIDs
    }
}
