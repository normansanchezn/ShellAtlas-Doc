import Foundation
import SD_Domain

enum DocumentOwnerMapper {
    static func toDomain(_ dto: DocumentOwnerDTO) -> DocumentOwner {
        DocumentOwner(
            id: dto.id,
            name: dto.name,
            team: dto.team,
            email: dto.email,
            documentIDs: dto.documentIDs
        )
    }
}
