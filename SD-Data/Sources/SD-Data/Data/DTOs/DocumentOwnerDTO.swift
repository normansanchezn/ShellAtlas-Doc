import Foundation

struct DocumentOwnerDTO: Codable, Sendable {
    let id: String
    let name: String
    let team: String
    let email: String
    let documentIDs: [String]
}
