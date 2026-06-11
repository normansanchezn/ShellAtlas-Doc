import Foundation

public protocol ReleaseRepository: Sendable {
    func getReleases() async throws -> [ReleaseNote]
}
