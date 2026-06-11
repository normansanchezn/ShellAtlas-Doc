//
//  KnowledgeSignal.swift
//  DS-Core
//
//  Created by Norman Sánchez on 05/06/26.
//

import Foundation

public struct KnowledgeSignal: Identifiable, Sendable {
    public let id: String
    public let type: SignalType
    public let title: String
    public let date: Date
    public let description: String
    public let relatedDocumentIDs: [String]

    public enum SignalType: String, Sendable, Codable {
        case ticket
        case commit
        case release
        case workflow
    }

    public init(
        id: String,
        type: SignalType,
        title: String,
        date: Date,
        description: String,
        relatedDocumentIDs: [String]
    ) {
        self.id = id
        self.type = type
        self.title = title
        self.date = date
        self.description = description
        self.relatedDocumentIDs = relatedDocumentIDs
    }
}
