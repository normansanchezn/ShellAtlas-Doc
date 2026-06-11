import Foundation
import SD_Domain

enum ReleaseNoteMapper {
    static func toDomain(_ dto: ReleaseNoteDTO) -> ReleaseNote {
        ReleaseNote(
            id: dto.id,
            version: dto.version,
            platform: Platform(rawValue: dto.platform) ?? .crossPlatform,
            releaseDate: dto.releaseDate,
            highlights: dto.highlights,
            keywords: dto.keywords,
            relatedDocumentIDs: dto.relatedDocumentIDs
        )
    }
}
