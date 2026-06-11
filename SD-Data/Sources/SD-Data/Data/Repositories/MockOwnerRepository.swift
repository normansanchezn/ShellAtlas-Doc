import Foundation
import SD_Domain

public struct MockOwnerRepository: OwnerRepository {
    public init() {}
    public func getOwners() async throws -> [DocumentOwner] {
        let dtos: [DocumentOwnerDTO] = try MockJSONLoader.load("owners")
        return dtos.map { DocumentOwnerMapper.toDomain($0) }
    }
}
