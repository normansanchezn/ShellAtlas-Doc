import Foundation
import SD_Domain

struct MockJSONLoader {
    static func load<T: Decodable>(_ filename: String) throws -> T {
        // Search all loaded bundles so the loader works in both app and test targets
        let url = Bundle.allBundles
            .compactMap { $0.url(forResource: filename, withExtension: "json") }
            .first
        guard let url else {
            throw DomainError.jsonNotFound(filename)
        }
        do {
            let data = try Data(contentsOf: url)
            let decoder = JSONDecoder()
            decoder.dateDecodingStrategy = .iso8601
            return try decoder.decode(T.self, from: data)
        } catch let error as DomainError {
            throw error
        } catch {
            throw DomainError.decodingFailed(error.localizedDescription)
        }
    }
}
