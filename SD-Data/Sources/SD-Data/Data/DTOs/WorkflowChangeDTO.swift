import Foundation

struct WorkflowChangeDTO: Codable, Sendable {
    let id: String
    let workflowName: String
    let changedAt: Date
    let description: String
    let changedFiles: [String]
    let relatedDocumentIDs: [String]
}
