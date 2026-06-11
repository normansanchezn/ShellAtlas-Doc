import Foundation

public enum DocumentationReviewPolicy {
    public static let staleReviewInterval: TimeInterval = 365 * 24 * 60 * 60

    public static func effectiveStatus(
        for document: DocumentationDocument,
        now: Date = Date()
    ) -> DocumentationStatus {
        updatesPending(document, now: now) ? .updatesPending : document.attributes.status
    }

    public static func updatesPending(
        _ document: DocumentationDocument,
        now: Date = Date()
    ) -> Bool {
        document.attributes.status == .updatesPending
            || document.attributes.status == .outdated
            || isStale(document, now: now)
    }

    public static func updateReason(
        for document: DocumentationDocument,
        now: Date = Date()
    ) -> String? {
        if document.attributes.status == .updatesPending {
            return "Marked for update"
        }
        if document.attributes.status == .outdated {
            return "Outdated content"
        }
        if isStale(document, now: now) {
            return "Last updated more than 1 year ago"
        }
        return nil
    }

    public static func isStale(
        _ document: DocumentationDocument,
        now: Date = Date()
    ) -> Bool {
        now.timeIntervalSince(document.attributes.lastUpdated) > staleReviewInterval
    }
}
