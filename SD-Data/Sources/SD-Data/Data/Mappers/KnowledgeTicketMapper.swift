import Foundation
import SD_Domain

enum KnowledgeTicketMapper {
    static func toDomain(_ dto: KnowledgeTicketDTO) -> KnowledgeTicket {
        KnowledgeTicket(
            id: dto.id,
            title: dto.title,
            type: TicketType(rawValue: dto.type) ?? .task,
            status: TicketStatus(rawValue: dto.status) ?? .open,
            closedDate: dto.closedDate,
            description: dto.description,
            platform: Platform(rawValue: dto.platform) ?? .crossPlatform,
            relatedDocumentIDs: dto.relatedDocumentIDs
        )
    }
}
