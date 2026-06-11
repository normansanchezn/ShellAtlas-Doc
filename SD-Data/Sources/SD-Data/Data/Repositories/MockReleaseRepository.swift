import Foundation
import SD_Domain

public struct MockReleaseRepository: ReleaseRepository {
    public init() {}
    public func getReleases() async throws -> [ReleaseNote] {
        let dtos: [ReleaseNoteDTO] = try MockJSONLoader.load("releases")
        return dtos.map { ReleaseNoteMapper.toDomain($0) }
    }
}
