import Foundation

public struct DocumentOwner: Identifiable, Sendable {
    public let id: String
    public let name: String
    public let team: String
    public let email: String
    public let documentIDs: [String]
    
    public init(id: String, name: String, team: String, email: String, documentIDs: [String]) {
        self.id = id
        self.name = name
        self.team = team
        self.email = email
        self.documentIDs = documentIDs
    }
}
