//
//  DocumentHealthRecommendation.swift
//  DS-Core
//
//  Created by Norman Sánchez on 05/06/26.
//

public enum DocumentHealthRecommendation: String, CaseIterable, Sendable, Codable {
    case upToDate
    case reviewRecommended
    case reviewRequired
    case criticalReview

    public var displayName: String {
        switch self {
        case .upToDate: "Up to Date"
        case .reviewRecommended: "Review Recommended"
        case .reviewRequired: "Review Required"
        case .criticalReview: "Critical Review"
        }
    }

    public var shortLabel: String {
        switch self {
        case .upToDate: "OK"
        case .reviewRecommended: "Review Soon"
        case .reviewRequired: "Needs Review"
        case .criticalReview: "Critical"
        }
    }
}
