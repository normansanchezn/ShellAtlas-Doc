import Foundation

public struct ReleaseNote: Identifiable, Sendable {
    public let id: String
    public let version: String
    public let platform: Platform
    public let releaseDate: Date
    public let highlights: [String]
    public let keywords: [String]
    public let relatedDocumentIDs: [String]
    
    public init(id: String, version: String, platform: Platform, releaseDate: Date, highlights: [String], keywords: [String], relatedDocumentIDs: [String]) {
        self.id = id
        self.version = version
        self.platform = platform
        self.releaseDate = releaseDate
        self.highlights = highlights
        self.keywords = keywords
        self.relatedDocumentIDs = relatedDocumentIDs
    }
}
