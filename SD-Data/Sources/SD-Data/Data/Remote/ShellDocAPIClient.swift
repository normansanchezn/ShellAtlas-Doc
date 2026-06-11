import Foundation
import SD_Domain

public struct ShellDocAPIConfiguration: Sendable {
    public let baseURL: URL
    public let bearerToken: String?

    public init(baseURL: URL, bearerToken: String? = nil) {
        self.baseURL = baseURL
        self.bearerToken = bearerToken
    }
}

public protocol ShellDocAPIClientProtocol: Sendable {
    func request<Response: Decodable>(
        _ path: String,
        method: String,
        queryItems: [URLQueryItem],
        body: (any Encodable)?,
        responseType: Response.Type
    ) async throws -> Response

    func requestNoContent(
        _ path: String,
        method: String,
        body: (any Encodable)?
    ) async throws
}

public final class ShellDocAPIClient: ShellDocAPIClientProtocol, @unchecked Sendable {
    private let configuration: ShellDocAPIConfiguration
    private let urlSession: URLSession
    private let decoder: JSONDecoder
    private let encoder: JSONEncoder

    public init(configuration: ShellDocAPIConfiguration, urlSession: URLSession = .shared) {
        self.configuration = configuration
        self.urlSession = urlSession

        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .custom(Self.decodeDate)
        decoder.keyDecodingStrategy = .convertFromSnakeCase
        self.decoder = decoder

        let encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .iso8601
        encoder.keyEncodingStrategy = .convertToSnakeCase
        self.encoder = encoder
    }

    public func request<Response: Decodable>(
        _ path: String,
        method: String = "GET",
        queryItems: [URLQueryItem] = [],
        body: (any Encodable)? = nil,
        responseType: Response.Type = Response.self
    ) async throws -> Response {
        let request = try urlRequest(path, method: method, queryItems: queryItems, body: body)
        let requestID = UUID().uuidString
        let startedAt = Date()
        logRequestStarted(request, requestID: requestID, body: body)
        var responseData: Data?
        var receivedResponse: URLResponse?
        do {
            let (receivedData, response) = try await urlSession.data(for: request)
            responseData = receivedData
            receivedResponse = response
            try validate(response: response, data: receivedData)
            logRequestFinished(
                request,
                response: response,
                requestID: requestID,
                startedAt: startedAt,
                responseByteCount: receivedData.count,
                responsePreview: ShellDocRemoteLogger.responsePreview(from: receivedData)
            )
        } catch {
            logRequestFailed(
                request,
                response: receivedResponse,
                requestID: requestID,
                startedAt: startedAt,
                error: error,
                responsePreview: responseData.map { ShellDocRemoteLogger.responsePreview(from: $0) }
            )
            throw error
        }
        do {
            guard let responseData else {
                throw DomainError.remoteRequestFailed("Missing response data")
            }
            return try decoder.decode(Response.self, from: responseData)
        } catch {
            ShellDocRemoteLogger.log("response_decoding_failed", fields: [
                "request_id": requestID,
                "method": method,
                "path": request.url?.path,
                "error": error.localizedDescription,
                "response_bytes": responseData?.count ?? 0,
                "response_preview": responseData.map { ShellDocRemoteLogger.responsePreview(from: $0) }
            ])
            throw DomainError.decodingFailed(error.localizedDescription)
        }
    }

    public func requestNoContent(
        _ path: String,
        method: String,
        body: (any Encodable)? = nil
    ) async throws {
        let request = try urlRequest(path, method: method, queryItems: [], body: body)
        let requestID = UUID().uuidString
        let startedAt = Date()
        logRequestStarted(request, requestID: requestID, body: body)
        var receivedResponse: URLResponse?
        var responseData: Data?
        do {
            let (receivedData, response) = try await urlSession.data(for: request)
            receivedResponse = response
            responseData = receivedData
            try validate(response: response, data: receivedData)
            logRequestFinished(
                request,
                response: response,
                requestID: requestID,
                startedAt: startedAt,
                responseByteCount: receivedData.count,
                responsePreview: ShellDocRemoteLogger.responsePreview(from: receivedData)
            )
        } catch {
            logRequestFailed(
                request,
                response: receivedResponse,
                requestID: requestID,
                startedAt: startedAt,
                error: error,
                responsePreview: responseData.map { ShellDocRemoteLogger.responsePreview(from: $0) }
            )
            throw error
        }
    }

    private func urlRequest(
        _ path: String,
        method: String,
        queryItems: [URLQueryItem],
        body: (any Encodable)?
    ) throws -> URLRequest {
        var components = URLComponents(url: configuration.baseURL.appending(path: path), resolvingAgainstBaseURL: false)
        components?.queryItems = queryItems.isEmpty ? nil : queryItems
        guard let url = components?.url else {
            throw DomainError.remoteRequestFailed("Invalid API URL for path \(path)")
        }

        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        if let bearerToken = configuration.bearerToken, !bearerToken.isEmpty {
            request.setValue("Bearer \(bearerToken)", forHTTPHeaderField: "Authorization")
        }
        if let body {
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.httpBody = try encoder.encode(AnyEncodable(body))
        }
        return request
    }

    private func validate(response: URLResponse, data: Data) throws {
        guard let httpResponse = response as? HTTPURLResponse else {
            throw DomainError.remoteRequestFailed("Missing HTTP response")
        }
        guard (200..<300).contains(httpResponse.statusCode) else {
            let message = String(data: data, encoding: .utf8) ?? HTTPURLResponse.localizedString(forStatusCode: httpResponse.statusCode)
            throw DomainError.remoteRequestFailed("HTTP \(httpResponse.statusCode): \(message)")
        }
    }

    private func logRequestStarted(_ request: URLRequest, requestID: String, body: (any Encodable)?) {
        ShellDocRemoteLogger.log("request_started", fields: [
            "request_id": requestID,
            "backend": "ShellDoc Backend API",
            "method": request.httpMethod,
            "url": request.url?.absoluteString,
            "path": request.url?.path,
            "query": request.url.flatMap { URLComponents(url: $0, resolvingAgainstBaseURL: false)?.query },
            "headers": ShellDocRemoteLogger.redactedHeaders(from: request),
            "request_body": body == nil ? nil : [
                "present": true,
                "redacted": true,
                "reason": "Body can contain document markdown or enterprise metadata"
            ],
            "request_bytes": request.httpBody?.count ?? 0
        ])
    }

    private func logRequestFinished(
        _ request: URLRequest,
        response: URLResponse,
        requestID: String,
        startedAt: Date,
        responseByteCount: Int,
        responsePreview: String? = nil
    ) {
        let httpResponse = response as? HTTPURLResponse
        ShellDocRemoteLogger.log("request_finished", fields: [
            "request_id": requestID,
            "backend": "ShellDoc Backend API",
            "method": request.httpMethod,
            "url": request.url?.absoluteString,
            "path": request.url?.path,
            "status_code": httpResponse?.statusCode,
            "duration_ms": Int(Date().timeIntervalSince(startedAt) * 1000),
            "response_bytes": responseByteCount,
            "response_preview": responsePreview
        ])
    }

    private func logRequestFailed(
        _ request: URLRequest,
        response: URLResponse?,
        requestID: String,
        startedAt: Date,
        error: Error,
        responsePreview: String?
    ) {
        let httpResponse = response as? HTTPURLResponse
        ShellDocRemoteLogger.log("request_failed", fields: [
            "request_id": requestID,
            "backend": "ShellDoc Backend API",
            "method": request.httpMethod,
            "url": request.url?.absoluteString,
            "path": request.url?.path,
            "status_code": httpResponse?.statusCode,
            "duration_ms": Int(Date().timeIntervalSince(startedAt) * 1000),
            "error": error.localizedDescription,
            "response_preview": responsePreview
        ])
    }

    private static func decodeDate(decoder: Decoder) throws -> Date {
        let container = try decoder.singleValueContainer()
        let value = try container.decode(String.self)
        let fractionalFormatter = ISO8601DateFormatter()
        fractionalFormatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        let standardFormatter = ISO8601DateFormatter()
        standardFormatter.formatOptions = [.withInternetDateTime]
        if let date = fractionalFormatter.date(from: value) ?? standardFormatter.date(from: value) {
            return date
        }
        throw DecodingError.dataCorruptedError(in: container, debugDescription: "Invalid ISO8601 date: \(value)")
    }
}

private struct AnyEncodable: Encodable {
    private let encodeValue: (Encoder) throws -> Void

    init(_ value: any Encodable) {
        self.encodeValue = value.encode
    }

    func encode(to encoder: Encoder) throws {
        try encodeValue(encoder)
    }
}
