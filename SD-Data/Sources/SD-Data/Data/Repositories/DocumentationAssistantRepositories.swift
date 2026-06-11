import Foundation
import SD_Domain

public struct MockLocalLLMClient: LocalLLMClient {
    public init() {}

    public func generateAnswer(prompt: String) async throws -> String {
        guard !prompt.isEmpty else {
            return DocumentationAssistantDefaults.notEnoughInformationMessage
        }

        let sourceTitles = prompt
            .components(separatedBy: "\n")
            .filter { $0.hasPrefix("Source: ") }
            .map { $0.replacingOccurrences(of: "Source: ", with: "") }

        let sourceSummary = sourceTitles.isEmpty
            ? "the retrieved documentation"
            : sourceTitles.prefix(3).joined(separator: ", ")

        if prompt.requiresDiagramAnswer {
            let mockDiagram = prompt.mockMermaidDiagram
            if prompt.requiresSpanishAnswer {
                return """
                \(mockDiagram.spanish)

                ## Resumen

                Con base en la documentación local, el proceso relevante está descrito en \(sourceSummary).

                ## Flujo documentado

                1. Revisa el documento fuente citado.
                2. Sigue la secuencia documentada, sin agregar pasos que no estén confirmados.
                3. Valida cualquier handoff, build, QA o release contra las fuentes.

                ## Brechas de documentación

                No asumir owners, tickets, ramas o criterios adicionales si no aparecen en las fuentes.
                """
            } else {
                return """
                \(mockDiagram.english)

                ## Summary

                Based on local documentation, the relevant process is described in \(sourceSummary).

                ## Documented flow

                1. Review the cited source document.
                2. Follow the documented sequence without adding unconfirmed steps.
                3. Validate any handoff, build, QA, or release detail against the sources.

                ## Documentation gaps

                Do not assume extra owners, tickets, branches, or criteria unless they appear in the retrieved sources.
                """
            }
        }

        if prompt.requiresSpanishAnswer {
            return """
            ## Resumen

            Con base en la documentación local, la información relevante está en \(sourceSummary).

            ## Lo que confirma la documentación

            - La respuesta está fundamentada en las fuentes recuperadas.
            - Revisa las fuentes citadas para validar el detalle exacto del proceso.

            ## Brechas de documentación

            Si necesitas una confirmación más específica, la documentación debe explicitarla.
            """
        } else {
            return """
            ## Summary

            Based on local documentation, the relevant information is in \(sourceSummary).

            ## What the documentation confirms

            - The answer is grounded in the retrieved sources.
            - Review the cited sources for the exact process details.

            ## Documentation gaps

            If you need a more specific confirmation, the documentation must state it explicitly.
            """
        }
    }
}

private struct MockDiagram {
    let english: String
    let spanish: String
}

private extension String {
    var requiresDiagramAnswer: Bool {
        let normalized = folding(options: [.diacriticInsensitive, .caseInsensitive], locale: .current)
        return ["valid Mermaid diagram", "diagrama", "diagram", "flujo", "flow", "proceso", "process"].contains { normalized.contains($0.lowercased()) }
    }

    var requiresSpanishAnswer: Bool {
        contains("Response language:\n- Reply in Spanish") || contains("Respuesta en Spanish")
    }

    var mockMermaidDiagram: MockDiagram {
        let lower = lowercased()
        if lower.contains("sequencediagram") || lower.contains("sequencediagram") || lower.contains("sequencediagram\n") {
            return MockDiagram(
                english: """
                ```mermaid
                sequenceDiagram
                    participant User
                    participant System
                    participant Service
                    User->>System: Initiates action
                    System->>Service: Forwards request
                    Service-->>System: Returns result
                    System-->>User: Displays outcome
                ```
                """,
                spanish: """
                ```mermaid
                sequenceDiagram
                    participant Usuario
                    participant Sistema
                    participant Servicio
                    Usuario->>Sistema: Inicia acción
                    Sistema->>Servicio: Reenvía solicitud
                    Servicio-->>Sistema: Devuelve resultado
                    Sistema-->>Usuario: Muestra resultado
                ```
                """
            )
        }
        if lower.contains("statediagram") {
            return MockDiagram(
                english: """
                ```mermaid
                stateDiagram-v2
                    [*] --> Initial
                    Initial --> InProgress : process starts
                    InProgress --> Validated : validation passes
                    InProgress --> Failed : validation fails
                    Validated --> [*]
                    Failed --> Initial : retry
                ```
                """,
                spanish: """
                ```mermaid
                stateDiagram-v2
                    [*] --> Inicial
                    Inicial --> EnProceso : inicia proceso
                    EnProceso --> Validado : validación exitosa
                    EnProceso --> Fallido : validación falla
                    Validado --> [*]
                    Fallido --> Inicial : reintento
                ```
                """
            )
        }
        if lower.contains("flowchart lr") {
            return MockDiagram(
                english: """
                ```mermaid
                flowchart LR
                    A[Review documentation] --> B[Identify context]
                    B --> C[Apply process]
                    C --> D[Validate result]
                ```
                """,
                spanish: """
                ```mermaid
                flowchart LR
                    A[Revisar documentación] --> B[Identificar contexto]
                    B --> C[Aplicar proceso]
                    C --> D[Validar resultado]
                ```
                """
            )
        }
        return MockDiagram(
            english: """
            ```mermaid
            flowchart TD
                A[Review retrieved documentation] --> B[Identify the documented process]
                B --> C[Follow confirmed steps]
                C --> D[Validate handoff, QA, or release details with sources]
            ```
            """,
            spanish: """
            ```mermaid
            flowchart TD
                A[Revisar documentación recuperada] --> B[Identificar el proceso documentado]
                B --> C[Seguir los pasos confirmados]
                C --> D[Validar handoff, QA o release con las fuentes]
            ```
            """
        )
    }
}

public struct UnavailableLocalLLMClient: LocalLLMClient {
    public init() {}

    public func generateAnswer(prompt: String) async throws -> String {
        throw OllamaLocalLLMError.connectionFailed
    }
}

public final class MockDocumentationAssistantRepository: DocumentationAssistantRepository, @unchecked Sendable {
    private let askUseCase: AskDocumentationAssistantUseCase

    public init(documentationRepository: any DocumentationRepository = MockDocumentationRepository()) {
        let retrievalRepository = LocalDocumentationRetrievalRepository(documentationRepository: documentationRepository)
        let retrievalUseCase = RetrieveDocumentationSnippetsUseCase(repository: retrievalRepository)
        self.askUseCase = AskDocumentationAssistantUseCase(
            retrievalUseCase: retrievalUseCase,
            promptBuilder: DefaultDocumentationAssistantPromptBuilder(),
            localLLMClient: MockLocalLLMClient()
        )
    }

    public func ask(question: DocumentationQuestion) async throws -> DocumentationAnswer {
        try await askUseCase.execute(question: question)
    }
}

public final class OllamaDocumentationAssistantRepository: DocumentationAssistantRepository, @unchecked Sendable {
    private let askUseCase: AskDocumentationAssistantUseCase

    public init(
        documentationRepository: any DocumentationRepository,
        client: any LocalLLMClient
    ) {
        let retrievalRepository = LocalDocumentationRetrievalRepository(documentationRepository: documentationRepository)
        let retrievalUseCase = RetrieveDocumentationSnippetsUseCase(repository: retrievalRepository)
        self.askUseCase = AskDocumentationAssistantUseCase(
            retrievalUseCase: retrievalUseCase,
            promptBuilder: DefaultDocumentationAssistantPromptBuilder(),
            localLLMClient: client
        )
    }

    public func ask(question: DocumentationQuestion) async throws -> DocumentationAnswer {
        try await askUseCase.execute(question: question)
    }
}
