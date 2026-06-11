//
//  DocumentHealthResult.swift
//  DS-Core
//
//  Created by Norman Sánchez on 05/06/26.
//

import Foundation

public struct DocumentHealthResult: Sendable {
    public let healthScore: Int
    public let recommendation: DocumentHealthRecommendation
    public let reasons: [String]
    public let matchedSignals: [KnowledgeSignal]
    public let suggestedActions: [String]

    public init(
        healthScore: Int,
        recommendation: DocumentHealthRecommendation,
        reasons: [String],
        matchedSignals: [KnowledgeSignal],
        suggestedActions: [String]
    ) {
        self.healthScore = healthScore
        self.recommendation = recommendation
        self.reasons = reasons
        self.matchedSignals = matchedSignals
        self.suggestedActions = suggestedActions
    }
}
