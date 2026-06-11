import Foundation
import SD_Domain

public enum OllamaLocalLLMError: LocalizedError, Equatable, Sendable {
    case connectionFailed
    case modelUnavailable(String)
    case timeout
    case invalidResponse
    case emptyResponse
    case serverError(String)

    public var errorDescription: String? {
        switch self {
        case .connectionFailed:
            "Local Ollama is not available. Make sure Ollama is running on this Mac."
        case .modelUnavailable(let model):
            "The local Ollama model '\(model)' is not installed."
        case .timeout:
            "The local Ollama request timed out."
        case .invalidResponse:
            "Ollama returned an invalid response."
        case .emptyResponse:
            "Ollama returned an empty response."
        case .serverError(let message):
            message
        }
    }
}

public final class OllamaLocalLLMClient: LocalLLMClient, @unchecked Sendable {
    private let baseURL: URL
    private let model: String
    private let timeout: TimeInterval
    private let urlSession: URLSession

    public init(
        baseURL: URL = URL(string: DocumentationAssistantConfiguration.defaultOllamaBaseURL)!,
        model: String = "qwen2.5-coder:7b",
        timeout: TimeInterval = 60,
        urlSession: URLSession = .shared
    ) {
        self.baseURL = baseURL
        self.model = model
        self.timeout = timeout
        self.urlSession = urlSession
    }

    public func generateAnswer(prompt: String) async throws -> String {
        let endpoint = baseURL.appending(path: "api/generate")
        var request = URLRequest(url: endpoint, timeoutInterval: timeout)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONEncoder().encode(
            OllamaGenerateRequest(
                model: model,
                prompt: prompt,
                stream: false,
                options: OllamaGenerateOptions(numPredict: 4096)
            )
        )

        let requestID = UUID().uuidString
        let startedAt = Date()
        ShellDocOllamaLogger.log("request_started", fields: [
            "request_id": requestID,
            "base_url": baseURL.absoluteString,
            "endpoint": endpoint.absoluteString,
            "method": "POST",
            "model": model,
            "timeout_seconds": timeout,
            "prompt_char_count": prompt.count,
            "request_body_bytes": request.httpBody?.count ?? 0
        ])

        var responseByteCount = 0
        var responseData: Data?
        do {
            let (data, response) = try await urlSession.data(for: request)
            responseData = data
            responseByteCount = data.count
            guard let httpResponse = response as? HTTPURLResponse else {
                ShellDocOllamaLogger.log("request_failed", fields: [
                    "request_id": requestID,
                    "base_url": baseURL.absoluteString,
                    "endpoint": endpoint.absoluteString,
                    "method": "POST",
                    "model": model,
                    "duration_ms": Int(Date().timeIntervalSince(startedAt) * 1000),
                    "error": "invalid_response",
                    "response_bytes": data.count,
                    "response_preview": ShellDocOllamaLogger.responsePreview(from: data)
                ])
                throw OllamaLocalLLMError.invalidResponse
            }

            guard (200..<300).contains(httpResponse.statusCode) else {
                let serverError = mapServerError(data: data, statusCode: httpResponse.statusCode)
                ShellDocOllamaLogger.log("request_failed", fields: [
                    "request_id": requestID,
                    "base_url": baseURL.absoluteString,
                    "endpoint": endpoint.absoluteString,
                    "method": "POST",
                    "model": model,
                    "status_code": httpResponse.statusCode,
                    "duration_ms": Int(Date().timeIntervalSince(startedAt) * 1000),
                    "error": String(describing: serverError),
                    "response_bytes": data.count,
                    "response_preview": ShellDocOllamaLogger.responsePreview(from: data)
                ])
                throw serverError
            }

            let decoded = try JSONDecoder().decode(OllamaGenerateResponse.self, from: data)
            let answer = decoded.response.trimmingCharacters(in: .whitespacesAndNewlines)
            guard !answer.isEmpty else {
                ShellDocOllamaLogger.log("request_failed", fields: [
                    "request_id": requestID,
                    "base_url": baseURL.absoluteString,
                    "endpoint": endpoint.absoluteString,
                    "method": "POST",
                    "model": model,
                    "status_code": httpResponse.statusCode,
                    "duration_ms": Int(Date().timeIntervalSince(startedAt) * 1000),
                    "error": "empty_response",
                    "response_bytes": data.count,
                    "response_preview": ShellDocOllamaLogger.responsePreview(from: data)
                ])
                throw OllamaLocalLLMError.emptyResponse
            }

            ShellDocOllamaLogger.log("request_finished", fields: [
                "request_id": requestID,
                "base_url": baseURL.absoluteString,
                "endpoint": endpoint.absoluteString,
                "method": "POST",
                "model": model,
                "status_code": httpResponse.statusCode,
                "duration_ms": Int(Date().timeIntervalSince(startedAt) * 1000),
                "response_bytes": data.count,
                "answer_char_count": answer.count,
                "response_preview": answer
            ])
            return answer
        } catch let error as OllamaLocalLLMError {
            if error != .invalidResponse && error != .emptyResponse {
                ShellDocOllamaLogger.log("request_failed", fields: [
                    "request_id": requestID,
                    "base_url": baseURL.absoluteString,
                    "endpoint": endpoint.absoluteString,
                    "method": "POST",
                    "model": model,
                    "duration_ms": Int(Date().timeIntervalSince(startedAt) * 1000),
                    "error": String(describing: error),
                    "response_preview": nil
                ])
            }
            throw error
        } catch let error as URLError {
            ShellDocOllamaLogger.log("request_failed", fields: [
                "request_id": requestID,
                "base_url": baseURL.absoluteString,
                "endpoint": endpoint.absoluteString,
                "method": "POST",
                "model": model,
                "duration_ms": Int(Date().timeIntervalSince(startedAt) * 1000),
                "error": error.code.rawValue,
                "url_error_code": error.code.rawValue,
                "response_preview": nil
            ])
            switch error.code {
            case .timedOut:
                throw OllamaLocalLLMError.timeout
            case .cannotConnectToHost, .networkConnectionLost, .notConnectedToInternet, .cannotFindHost:
                throw OllamaLocalLLMError.connectionFailed
            default:
                throw OllamaLocalLLMError.connectionFailed
            }
        } catch is DecodingError {
            ShellDocOllamaLogger.log("response_decoding_failed", fields: [
                "request_id": requestID,
                "base_url": baseURL.absoluteString,
                "endpoint": endpoint.absoluteString,
                "method": "POST",
                "model": model,
                "duration_ms": Int(Date().timeIntervalSince(startedAt) * 1000),
                "response_bytes": responseByteCount,
                "response_preview": responseData.map { ShellDocOllamaLogger.responsePreview(from: $0) }
            ])
            throw OllamaLocalLLMError.invalidResponse
        } catch {
            ShellDocOllamaLogger.log("request_failed", fields: [
                "request_id": requestID,
                "base_url": baseURL.absoluteString,
                "endpoint": endpoint.absoluteString,
                "method": "POST",
                "model": model,
                "duration_ms": Int(Date().timeIntervalSince(startedAt) * 1000),
                "error": String(describing: error),
                "response_preview": nil
            ])
            throw OllamaLocalLLMError.connectionFailed
        }
    }

    private func mapServerError(data: Data, statusCode: Int) -> OllamaLocalLLMError {
        let decodedMessage = try? JSONDecoder().decode(OllamaErrorResponse.self, from: data).error
        let rawMessage = String(data: data, encoding: .utf8)?
            .trimmingCharacters(in: .whitespacesAndNewlines)
        let message = decodedMessage
            ?? rawMessage?.nilIfEmpty
            ?? HTTPURLResponse.localizedString(forStatusCode: statusCode)

        if message.localizedCaseInsensitiveContains("model") || message.localizedCaseInsensitiveContains("not found") {
            return .modelUnavailable(model)
        }

        return .serverError(message)
    }
}

private struct OllamaGenerateRequest: Encodable {
    let model: String
    let prompt: String
    let stream: Bool
    let options: OllamaGenerateOptions
}

private struct OllamaGenerateOptions: Encodable {
    let numPredict: Int

    enum CodingKeys: String, CodingKey {
        case numPredict = "num_predict"
    }
}

private struct OllamaGenerateResponse: Decodable {
    let response: String
}

private struct OllamaErrorResponse: Decodable {
    let error: String
}

private extension String {
    var nilIfEmpty: String? {
        isEmpty ? nil : self
    }
}
