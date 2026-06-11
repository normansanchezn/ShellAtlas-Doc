import Foundation

struct ReleaseNoteDTO: Codable, Sendable {
    let id: String
    let version: String
    let platform: String
    let releaseDate: Date
    let highlights: [String]
    let keywords: [String]
    let relatedDocumentIDs: [String]
}
