import Foundation
import SD_Domain

enum DocumentationSourceSystem: String, Sendable {
    case confluence
    case jira
    case azureDevOps
}

struct ExportedDocumentationRecord: Sendable {
    let sourceSystem: DocumentationSourceSystem
    let externalID: String
    let title: String
    let summary: String
    let bodyMarkdown: String
    let ownerDisplayName: String
    let moduleName: String
    let teamName: String
    let reviewStatus: DocumentationStatus
    let labels: [String]
    let createdAtISO8601: String
    let lastUpdatedISO8601: String
    let sourceURL: String
    let linkedJiraKeys: [String]
    let linkedAzureWorkItemIDs: [String]
}

enum DocumentationExportMock {
    static let exportedRecords: [ExportedDocumentationRecord] = [
        ExportedDocumentationRecord(
            sourceSystem: .confluence,
            externalID: "CONF-18492",
            title: "iOS Payment Sheet",
            summary: "Confluence export for the iOS payment sheet behavior and validation states.",
            bodyMarkdown: """
            # iOS Payment Sheet

            The iOS payment sheet collects payment confirmation details before a fuel transaction starts.

            ## Validation Rules

            - A default payment method is required.
            - Loyalty discounts should appear before tax.
            - The confirm button stays disabled while pricing is refreshing.

            ## Source Export

            This record represents a Confluence page export with linked Jira and Azure work items.
            """,
            ownerDisplayName: "Isabella Cruz",
            moduleName: "Payments",
            teamName: "iOS Shell App",
            reviewStatus: .published,
            labels: ["ios", "payments", "confluence-export"],
            createdAtISO8601: "2024-01-15T15:00:00Z",
            lastUpdatedISO8601: "2024-04-22T17:20:00Z",
            sourceURL: "https://confluence.internal/wiki/ios-payment-sheet",
            linkedJiraKeys: ["MOB-1842", "MOB-1910"],
            linkedAzureWorkItemIDs: ["84211"]
        ),
        ExportedDocumentationRecord(
            sourceSystem: .jira,
            externalID: "JIRA-MOB-2201",
            title: "Fuel Rewards Error States",
            summary: "Jira-backed runbook for loyalty error states surfaced during fuel rewards redemption.",
            bodyMarkdown: """
            # Fuel Rewards Error States

            This runbook describes what the mobile app should show when rewards redemption fails.

            ## User-Facing States

            - Reward unavailable.
            - Loyalty session expired.
            - Station does not support redemption.
            - Retryable backend error.

            ## QA Notes

            Validate each state with mocked API responses before release.
            """,
            ownerDisplayName: "Rafael Medina",
            moduleName: "Loyalty",
            teamName: "Mobile QA",
            reviewStatus: .updatesPending,
            labels: ["loyalty", "qa", "jira-export"],
            createdAtISO8601: "2025-08-06T10:30:00Z",
            lastUpdatedISO8601: "2026-02-02T12:45:00Z",
            sourceURL: "https://jira.internal/browse/MOB-2201",
            linkedJiraKeys: ["MOB-2201", "MOB-2264"],
            linkedAzureWorkItemIDs: []
        ),
        ExportedDocumentationRecord(
            sourceSystem: .azureDevOps,
            externalID: "ADO-78122",
            title: "Station Locator Cache Policy",
            summary: "Azure DevOps export for station locator cache behavior and refresh cadence.",
            bodyMarkdown: """
            # Station Locator Cache Policy

            Station data is cached to reduce startup latency and avoid repeated location searches.

            ## Cache Rules

            - Station search results expire after 24 hours.
            - Favorite stations refresh on app launch.
            - Location permission changes clear cached nearby results.

            ## Review Note

            This page is intentionally old in the mock export so the one-year review rule can be validated.
            """,
            ownerDisplayName: "Mina Okafor",
            moduleName: "Station Locator",
            teamName: "Backend Platform",
            reviewStatus: .published,
            labels: ["stations", "cache", "azure-export"],
            createdAtISO8601: "2023-03-18T09:10:00Z",
            lastUpdatedISO8601: "2024-02-09T14:35:00Z",
            sourceURL: "https://dev.azure.com/shell/mobile/_workitems/edit/78122",
            linkedJiraKeys: [],
            linkedAzureWorkItemIDs: ["78122", "79340"]
        )
    ]

    static var documents: [DocumentationDocument] {
        exportedRecords.map(mapToDocumentationDocument)
    }

    private static func mapToDocumentationDocument(_ record: ExportedDocumentationRecord) -> DocumentationDocument {
        let sourceTag = record.sourceSystem.rawValue
        let linkedSummary = linkedReferences(for: record)
        let content = """
        \(record.bodyMarkdown)

        ## Imported Metadata

        - Source: \(record.sourceSystem.rawValue)
        - External ID: \(record.externalID)
        - Source URL: \(record.sourceURL)
        \(linkedSummary)
        """

        return DocumentationDocument(
            id: "export-\(record.externalID.lowercased())",
            title: record.title,
            summary: record.summary,
            content: content,
            attributes: DocumentationAttributes(
                owner: record.ownerDisplayName,
                module: record.moduleName,
                team: record.teamName,
                status: record.reviewStatus,
                tags: Array(Set(record.labels + [sourceTag])).sorted(),
                lastUpdated: date(record.lastUpdatedISO8601),
                createdAt: date(record.createdAtISO8601)
            )
        )
    }

    private static func linkedReferences(for record: ExportedDocumentationRecord) -> String {
        var rows: [String] = []
        if !record.linkedJiraKeys.isEmpty {
            rows.append("- Jira: \(record.linkedJiraKeys.joined(separator: ", "))")
        }
        if !record.linkedAzureWorkItemIDs.isEmpty {
            rows.append("- Azure Work Items: \(record.linkedAzureWorkItemIDs.joined(separator: ", "))")
        }
        return rows.joined(separator: "\n")
    }

    private static func date(_ value: String) -> Date {
        ISO8601DateFormatter().date(from: value) ?? Date(timeIntervalSince1970: 0)
    }
}
