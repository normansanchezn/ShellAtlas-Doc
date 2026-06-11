import Foundation
import SD_Domain
import SD_DesignSystem

@Observable
@MainActor
final class AssistantViewModel {
    var messages: [AssistantMessage] = []
    var inputText = ""
    var isThinking = false
    var errorMessage: String?
    var responseLanguage: AssistantResponseLanguage = .english
    var availabilityState: AssistantAvailabilityState = .checking
    var selectedSource: DocumentationSource?

    var isLocalModelAvailable: Bool {
        if case .available = availabilityState { return true }
        return false
    }

    var isCheckingLocalModel: Bool {
        availabilityState == .checking
    }

    var statusMessage: String {
        switch availabilityState {
        case .checking: return "Verificando AI…"
        case .available(let model): return "Local AI is ready with \(model)."
        case .ollamaNotRunning: return "Ollama is not running. Start Ollama and try again."
        case .modelNotInstalled(let model): return "\(model) is not installed. Run: ollama pull \(model)"
        case .unavailable(let reason): return reason
        }
    }
    var selectedSourceDocument: DocumentationDocument?
    var isLoadingSelectedSource = false
    var selectedSourceErrorMessage: String?

    func send(useCase: any AskDocumentationAssistantUseCaseProtocol) async {
        let questionText = inputText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !questionText.isEmpty, !isThinking else { return }
        responseLanguage = AssistantResponseLanguage.detect(from: questionText)

        messages.append(AssistantMessage(role: .user, text: questionText))
        inputText = ""
        errorMessage = nil
        isThinking = true

        do {
            let answer = try await useCase.execute(question: DocumentationQuestion(text: questionText))
            messages.append(
                AssistantMessage(
                    role: .assistant,
                    text: answer.text,
                    sources: answer.sources
                )
            )
        } catch {
            errorMessage = error.localizedDescription
            availabilityState = .unavailable(reason: error.localizedDescription)
            messages.append(
                AssistantMessage(
                    role: .assistant,
                    text: responseLanguage.assistantUnavailableMessage
                )
            )
        }

        isThinking = false
    }

    func clearConversation() {
        messages = []
        errorMessage = nil
        selectedSource = nil
        selectedSourceDocument = nil
        selectedSourceErrorMessage = nil
    }

    func selectSource(_ source: DocumentationSource, useCase: any GetDocumentationDocumentDetailUseCaseProtocol) async {
        selectedSource = source
        selectedSourceDocument = nil
        selectedSourceErrorMessage = nil
        isLoadingSelectedSource = true
        defer { isLoadingSelectedSource = false }

        do {
            selectedSourceDocument = try await useCase.execute(id: source.documentId)
        } catch {
            selectedSourceErrorMessage = responseLanguage.sourceUnavailableMessage
        }
    }

    func closeSelectedSource() {
        selectedSource = nil
        selectedSourceDocument = nil
        selectedSourceErrorMessage = nil
    }

    func refreshStatus(useCase: any CheckDocumentationAssistantStatusUseCaseProtocol, usesOllamaAssistant: Bool) async {
        availabilityState = .checking

        guard usesOllamaAssistant else {
            availabilityState = .available(model: "Demo local mock")
            return
        }

        let status = await useCase.execute()
        availabilityState = status.availabilityState
    }
}

enum AssistantResponseLanguage {
    case english
    case spanish

    static func detect(from text: String) -> AssistantResponseLanguage {
        let normalized = text
            .folding(options: [.diacriticInsensitive, .caseInsensitive], locale: .current)
            .lowercased()
        let terms = Set(normalized.components(separatedBy: CharacterSet.alphanumerics.inverted))
        let spanishSignals = [
            "que", "cual", "cuantos", "cuantas", "hay", "mas", "deberia", "proceso",
            "flujo", "diagrama", "pregunta", "documentacion", "respuesta", "como",
            "donde", "cuando", "porque", "explica", "responde", "espanol"
        ]
        if text.contains("¿") || text.contains("¡") || spanishSignals.contains(where: { terms.contains($0) }) {
            return .spanish
        }
        return .english
    }

    var sourcesTitle: String {
        switch self {
        case .english: "Sources"
        case .spanish: "Fuentes"
        }
    }

    var sourceDocumentTitle: String {
        switch self {
        case .english: "Source document"
        case .spanish: "Documento fuente"
        }
    }

    var assistantUnavailableMessage: String {
        switch self {
        case .english: "The AI assistant is not available right now. Try again."
        case .spanish: "El asistente AI no está disponible por el momento. Inténtalo de nuevo."
        }
    }

    var sourceUnavailableTitle: String {
        switch self {
        case .english: "Source unavailable"
        case .spanish: "Fuente no disponible"
        }
    }

    var sourceUnavailableMessage: String {
        switch self {
        case .english: "This source document could not be opened. The assistant may have retrieved it from the documentation index, but the reader could not load the document record."
        case .spanish: "No se pudo abrir este documento fuente. El asistente pudo recuperarlo desde el índice de documentación, pero el lector no pudo cargar el registro del documento."
        }
    }

    var retryTitle: String {
        switch self {
        case .english: "Try again"
        case .spanish: "Intentar de nuevo"
        }
    }

    var openSourceAccessibilityPrefix: String {
        switch self {
        case .english: "Open source document"
        case .spanish: "Abrir documento fuente"
        }
    }

    var loadingSourceTitle: String {
        switch self {
        case .english: "Opening source"
        case .spanish: "Abriendo fuente"
        }
    }

    var loadingSourceMessage: String {
        switch self {
        case .english: "Loading the referenced document content."
        case .spanish: "Cargando el contenido del documento referenciado."
        }
    }
}

extension AssistantMessageRole {
    var designSystemRole: SDAssistantMessageRole {
        switch self {
        case .user:
            .user
        case .assistant:
            .assistant
        case .system:
            .system
        }
    }
}

extension DocumentationSource {
    var designSystemSource: SDAssistantSource {
        SDAssistantSource(
            id: id,
            documentId: documentId,
            title: title,
            sectionTitle: sectionTitle
        )
    }
}
