///
///S D-Data
///
///

import Foundation
import SD_Domain

public struct MockTicketRepository: TicketRepository {
    
    public init() {}
    public func getTickets() async throws -> [KnowledgeTicket] {
        let dtos: [KnowledgeTicketDTO] = try MockJSONLoader.load("tickets")
        return dtos.map { KnowledgeTicketMapper.toDomain($0) }
    }
}
