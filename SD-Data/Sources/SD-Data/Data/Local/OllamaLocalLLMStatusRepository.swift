import Foundation
import SD_Domain

public final class OllamaLocalLLMStatusRepository: LocalLLMStatusRepository, @unchecked Sendable {
    private let baseURL: URL
    private let model: String
    private let timeout: TimeInterval
    private let urlSession: URLSession

    public init(
        baseURL: URL = URL(string: DocumentationAssistantConfiguration.defaultOllamaBaseURL)!,
        model: String = DocumentationAssistantConfiguration.defaultOllamaModel,
        timeout: TimeInterval = 10,
        urlSession: URLSession = .shared
    ) {
        self.baseURL = baseURL
        self.model = model
        self.timeout = timeout
        self.urlSession = urlSession
    }

    public func checkStatus() async -> DocumentationAssistantStatus {
        let endpoint = baseURL.appending(path: "api/tags")
        var request = URLRequest(url: endpoint, timeoutInterval: timeout)
        request.httpMethod = "GET"
        let requestID = UUID().uuidString
        let startedAt = Date()

        ShellDocOllamaLogger.log("status_check_started", fields: [
            "request_id": requestID,
            "base_url": baseURL.absoluteString,
            "endpoint": endpoint.absoluteString,
            "method": "GET",
            "model": model,
            "timeout_seconds": timeout
        ])

        do {
            let (data, response) = try await urlSession.data(for: request)
            guard let httpResponse = response as? HTTPURLResponse, (200..<300).contains(httpResponse.statusCode) else {
                ShellDocOllamaLogger.log("status_check_failed", fields: [
                    "request_id": requestID,
                    "base_url": baseURL.absoluteString,
                    "endpoint": endpoint.absoluteString,
                    "method": "GET",
                    "model": model,
                    "duration_ms": Int(Date().timeIntervalSince(startedAt) * 1000),
                    "error": "non_2xx_response",
                    "response_bytes": data.count,
                    "response_preview": ShellDocOllamaLogger.responsePreview(from: data)
                ])
                return DocumentationAssistantStatus(
                    isOllamaRunning: false,
                    isModelInstalled: false,
                    modelName: model,
                    message: "Local Ollama is not available. Make sure Ollama is running on this Mac."
                )
            }

            let decoded = try JSONDecoder().decode(OllamaTagsResponse.self, from: data)
            let installedModels = decoded.models.map(\.name)
            let isModelInstalled = installedModels.contains(model)

            ShellDocOllamaLogger.log("status_check_finished", fields: [
                "request_id": requestID,
                "base_url": baseURL.absoluteString,
                "endpoint": endpoint.absoluteString,
                "method": "GET",
                "model": model,
                "status_code": httpResponse.statusCode,
                "duration_ms": Int(Date().timeIntervalSince(startedAt) * 1000),
                "is_ollama_running": true,
                "is_model_installed": isModelInstalled,
                "response_bytes": data.count
            ])

            return DocumentationAssistantStatus(
                isOllamaRunning: true,
                isModelInstalled: isModelInstalled,
                modelName: model,
                message: isModelInstalled
                    ? "Ollama is ready with \(model)."
                : "Ollama is running, but \(model) is not installed."
            )
        } catch {
            ShellDocOllamaLogger.log("status_check_failed", fields: [
                "request_id": requestID,
                "base_url": baseURL.absoluteString,
                "endpoint": endpoint.absoluteString,
                "method": "GET",
                "model": model,
                "duration_ms": Int(Date().timeIntervalSince(startedAt) * 1000),
                "error": String(describing: error),
                "response_preview": nil
            ])
            return DocumentationAssistantStatus(
                isOllamaRunning: false,
                isModelInstalled: false,
                modelName: model,
                message: "Local Ollama is not available. Make sure Ollama is running on this Mac."
            )
        }
    }
}

public struct MockLocalLLMStatusRepository: LocalLLMStatusRepository {
    private let status: DocumentationAssistantStatus

    public init(
        status: DocumentationAssistantStatus = DocumentationAssistantStatus(
            isOllamaRunning: false,
            isModelInstalled: false,
            modelName: DocumentationAssistantConfiguration.defaultOllamaModel,
            message: "Demo assistant is active."
        )
    ) {
        self.status = status
    }

    public func checkStatus() async -> DocumentationAssistantStatus {
        status
    }
}

private struct OllamaTagsResponse: Decodable {
    let models: [OllamaModel]
}

private struct OllamaModel: Decodable {
    let name: String
}
