import Foundation

struct KnowledgeDocumentDTO: Codable, Sendable {
    let id: String
    let title: String
    let type: String
    let area: String
    let platform: String
    let status: String
    let confidence: String
    let owner: String
    let mainContact: String
    let branches: [String]
    let relatedTools: [String]
    let relatedRepositories: [String]
    let summary: String
    let content: String
    let tags: [String]
    let lastValidated: Date
    let nextReview: Date
    let lastUpdated: Date
    let reviewFrequency: String
    let aiReviewPriority: String
    let relatedTicketIDs: [String]
    let relatedCommitIDs: [String]
    let relatedReleaseIDs: [String]
    let relatedWorkflowIDs: [String]
    let aiUpdateSignals: [String]
    let openAIQuestions: [String]
    let suggestedActions: [String]
    let mermaidDiagram: String?
}
