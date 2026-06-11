import Foundation

public struct KnowledgeTicket: Identifiable, Sendable {
    public let id: String
    public let title: String
    public let type: TicketType
    public let status: TicketStatus
    public let closedDate: Date?
    public let description: String
    public let platform: Platform
    public let relatedDocumentIDs: [String]
    
    public init(id: String, title: String, type: TicketType, status: TicketStatus, closedDate: Date?, description: String, platform: Platform, relatedDocumentIDs: [String]) {
        self.id = id
        self.title = title
        self.type = type
        self.status = status
        self.closedDate = closedDate
        self.description = description
        self.platform = platform
        self.relatedDocumentIDs = relatedDocumentIDs
    }
}
