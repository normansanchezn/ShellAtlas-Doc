import Foundation

public protocol OwnerRepository: Sendable {
    func getOwners() async throws -> [DocumentOwner]
}
