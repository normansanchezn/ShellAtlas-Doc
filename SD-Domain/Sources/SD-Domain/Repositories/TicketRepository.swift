import Foundation

public protocol TicketRepository: Sendable {
    func getTickets() async throws -> [KnowledgeTicket]
}
