import Foundation

struct KnowledgeTicketDTO: Codable, Sendable {
    let id: String
    let title: String
    let type: String
    let status: String
    let closedDate: Date?
    let description: String
    let platform: String
    let relatedDocumentIDs: [String]
}
