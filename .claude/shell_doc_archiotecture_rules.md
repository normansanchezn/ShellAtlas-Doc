# ShellDoc — Local Ollama Integration Architecture Rules

Read `AI_Rules_ShellDoc.md` first.

This document defines the architecture rules and implementation prompt for adding **local Ollama-powered documentation chat** to ShellDoc.

The goal is to let ShellDoc answer questions using local documentation without sending Shell documentation, code, process notes, tickets, commits, or business context to external AI providers.

---

## 1. Product Goal

ShellDoc should support a local AI assistant that answers questions based on the documentation stored inside the app.

The assistant must use a **RAG-style architecture**:

```text
Local Documentation
        ↓
Local Retrieval
        ↓
Relevant Snippets
        ↓
Prompt Builder
        ↓
Local Ollama Adapter
        ↓
Assistant Answer + Sources
```

The model should not be trained or fine-tuned with Shell documentation.

The app should retrieve the most relevant documentation snippets and send only that context to a local Ollama instance.

---

## 2. Privacy Rules

The integration must be local-first and privacy-safe.

Do not send documentation to:

- OpenAI
- Claude
- Gemini
- Azure OpenAI
- GitHub Copilot
- Remote APIs
- External search services
- Analytics tools
- Cloud vector databases

For this feature, all AI communication must happen through a local Ollama endpoint only.

Expected local endpoint:

```text
http://localhost:11434
```

The app must not expose Ollama to the public internet.

The app must not require API keys.

The app must not store prompts or answers outside local mock storage unless explicitly implemented later.

The app must clearly separate:

- Local documentation content
- Retrieved snippets
- Prompt construction
- Model response
- Displayed answer
- Source references

---

## 3. Security Rules

Do not give the local assistant access to the filesystem.

Do not allow the assistant to execute terminal commands.

Do not allow the assistant to call tools.

Do not allow the assistant to browse the internet.

Do not allow the assistant to mutate documentation directly.

The assistant can suggest edits, but it must not automatically update documents.

Any document update must go through the existing ShellDoc editor flow.

The assistant must never ask for or store:

- Passwords
- Tokens
- SSH keys
- API keys
- Personal banking information
- Complete card numbers
- Private employee information
- Production secrets
- Confidential credentials

---

## 4. Clean Architecture Rules

Use:

- SwiftUI
- MVVM
- Clean Architecture
- Repository Pattern
- UseCases
- Dependency Injection
- Async/Await

Respect module boundaries:

```text
SD-Presentation → SD-Domain ← SD-Data
```

`SD-Domain` must contain:

- Entities
- Value objects
- Repository protocols
- Use case protocols
- Use case implementations
- Prompt policy abstractions

`SD-Data` must contain:

- Mock repositories
- Local retrieval implementation
- Ollama client implementation
- DTOs
- Mappers
- Local in-memory data sources

`SD-Presentation` must contain:

- SwiftUI views
- ViewModels
- UI state
- User actions
- Loading, empty, and error states

`SD-DesignSystem` must contain:

- Reusable UI components
- Chat bubbles
- Source chips
- Loading views
- Error views
- Input components

Presentation must never call Ollama directly.

Presentation must never access Data repositories directly.

SwiftUI views must never build prompts.

SwiftUI views must never perform retrieval logic.

SwiftUI views must never parse Ollama responses.

---

## 5. Dependency Direction

Allowed:

```text
Presentation → Domain
Data → Domain
App Composition Root → Domain + Data + Presentation
```

Not allowed:

```text
Domain → Data
Domain → Presentation
Presentation → Data
DesignSystem → Data
DesignSystem → Domain business rules
```

---

## 6. RAG Architecture Rules

The assistant must not answer from the model alone.

The required flow is:

```text
1. User asks a question.
2. App searches local documentation.
3. App retrieves top relevant snippets.
4. App builds a strict prompt using those snippets.
5. App sends prompt to local Ollama.
6. Ollama returns an answer.
7. App displays the answer with source documents.
```

If no relevant snippets are found, the assistant must say that the documentation does not contain enough information.

The assistant must not invent:

- Processes
- Owners
- Tickets
- Commits
- Business rules
- Release instructions
- QA steps
- Technical decisions

---

## 7. Retrieval Rules

For the MVP, implement simple local retrieval using plain text scoring.

Search should evaluate:

- Document title
- Document summary
- Document content
- Tags
- Module
- Team
- Owner
- Status

Suggested scoring:

```text
Exact title match: +50
Partial title match: +35
Tag match: +30
Module match: +20
Summary match: +15
Content match: +10
Owner/team match: +5
```

Return the top 3 to 5 snippets.

Do not implement embeddings yet.

Do not use external vector databases.

Do not call external semantic search APIs.

---

## 8. Prompt Builder Rules

Prompt building must live outside SwiftUI views.

Create a dedicated prompt builder in the Domain or Data boundary depending on the existing architecture.

Recommended abstraction:

```swift
public protocol DocumentationAssistantPromptBuilder {
    func buildPrompt(
        question: String,
        snippets: [DocumentationSnippet]
    ) -> String
}
```

The prompt must force grounded answers.

Base prompt:

```text
You are ShellDoc Assistant.

You answer questions only using the provided documentation context.

Rules:
- Use only the context below.
- If the answer is not present in the context, say the documentation does not contain enough information.
- Do not invent owners, tickets, commits, release notes, QA steps, APIs, business rules, or implementation details.
- Mention the source document title when useful.
- Keep the answer clear, technical, and concise.
- If the user asks for an update, suggest a draft change but do not modify the document automatically.

Documentation Context:
{{retrieved_context}}

User Question:
{{question}}

Answer:
```

---

## 9. Ollama Adapter Rules

Create a local LLM client abstraction.

```swift
public protocol LocalLLMClient {
    func generateAnswer(prompt: String) async throws -> String
}
```

Create an Ollama implementation in `SD-Data`:

```swift
public final class OllamaLocalLLMClient: LocalLLMClient
```

The client should call a local Ollama server.

Expected endpoint:

```text
POST http://localhost:11434/api/generate
```

Expected payload:

```json
{
  "model": "qwen2.5-coder:7b",
  "prompt": "...",
  "stream": false
}
```

The model name must be configurable.

Do not hardcode the model name inside SwiftUI views.

Recommended default models:

```text
qwen2.5-coder:7b — technical documentation and code-oriented answers
llama3.2:3b — lightweight general answers
```

The app must gracefully handle:

- Ollama not running
- Model not installed
- Timeout
- Invalid response
- Empty response
- Local connection failure

---

## 10. Domain Entities

Create or extend entities for the assistant feature.

Suggested entities:

```swift
public struct DocumentationQuestion: Equatable {
    public let text: String
}

public struct DocumentationAnswer: Equatable {
    public let text: String
    public let sources: [DocumentationSource]
    public let confidence: DocumentationAnswerConfidence
}

public enum DocumentationAnswerConfidence: String, Equatable {
    case high
    case medium
    case low
    case notEnoughInformation
}

public struct DocumentationSnippet: Identifiable, Equatable {
    public let id: String
    public let documentId: String
    public let documentTitle: String
    public let text: String
    public let score: Double
    public let sectionTitle: String?
}

public struct DocumentationSource: Identifiable, Equatable {
    public let id: String
    public let documentId: String
    public let title: String
    public let sectionTitle: String?
}

public struct AssistantMessage: Identifiable, Equatable {
    public let id: String
    public let role: AssistantMessageRole
    public let text: String
    public let sources: [DocumentationSource]
    public let createdAt: Date
}

public enum AssistantMessageRole: Equatable {
    case user
    case assistant
    case system
}
```

---

## 11. Repository Protocols

Create repository protocols in `SD-Domain`.

```swift
public protocol DocumentationRetrievalRepository {
    func retrieveRelevantSnippets(
        for question: DocumentationQuestion,
        limit: Int
    ) async throws -> [DocumentationSnippet]
}

public protocol DocumentationAssistantRepository {
    func ask(question: DocumentationQuestion) async throws -> DocumentationAnswer
}
```

---

## 12. Use Cases

Create use cases in `SD-Domain`.

```swift
public protocol RetrieveDocumentationSnippetsUseCaseProtocol {
    func execute(question: DocumentationQuestion, limit: Int) async throws -> [DocumentationSnippet]
}

public protocol AskDocumentationAssistantUseCaseProtocol {
    func execute(question: DocumentationQuestion) async throws -> DocumentationAnswer
}
```

Implement:

```swift
public final class RetrieveDocumentationSnippetsUseCase: RetrieveDocumentationSnippetsUseCaseProtocol
public final class AskDocumentationAssistantUseCase: AskDocumentationAssistantUseCaseProtocol
```

`AskDocumentationAssistantUseCase` should coordinate:

```text
question → retrieval → prompt builder → local LLM client → answer + sources
```

---

## 13. Data Implementations

Create in `SD-Data`:

```swift
LocalDocumentationRetrievalRepository
OllamaLocalLLMClient
OllamaDocumentationAssistantRepository
MockLocalLLMClient
MockDocumentationAssistantRepository
```

Use `MockLocalLLMClient` for previews and tests.

Use `OllamaLocalLLMClient` only in the app composition root when local Ollama mode is enabled.

The first implementation may support both:

```text
Mock mode: deterministic answer from snippets
Ollama mode: local model response from prompt
```

---

## 14. Presentation Requirements

Create:

```swift
DocumentationAssistantView
DocumentationAssistantViewModel
```

The assistant screen should include:

- Message list
- User input field
- Send button
- Loading state
- Error state
- Empty state
- Source document references
- Clear conversation action
- Local model status indicator

The assistant must show sources used for an answer.

A source reference should include:

- Document title
- Optional section title
- Document ID

The UI must make it clear when the answer is based on local documentation.

Suggested empty state:

```text
Ask something about the documentation.
```

Suggested offline/Ollama error:

```text
Local Ollama is not available. Make sure Ollama is running on this Mac.
```

---

## 15. Design System Components

Create reusable components if they do not exist yet:

```swift
SDAssistantMessageBubble
SDAssistantInputBar
SDAssistantSourceChip
SDAssistantSourcesPanel
SDLocalModelStatusBadge
SDTypingIndicator
```

Keep these generic and reusable.

Do not put business logic inside Design System components.

---

## 16. Editor Integration Rules

The assistant may suggest documentation updates, but must not save changes automatically.

If the assistant suggests an update, the user must explicitly choose to open the editor.

Future flow:

```text
Assistant Answer
    ↓
Suggested Update Draft
    ↓
Open Documentation Editor
    ↓
User reviews and edits
    ↓
User saves manually
```

Do not implement automatic document mutation in this feature.

---

## 17. Testing Rules

Add tests for:

- Retrieval scoring
- Snippet ranking
- Empty retrieval fallback
- Prompt builder output
- Ask assistant use case with mock LLM
- Ollama client error mapping if practical

Do not require Ollama to be running for unit tests.

Tests must use `MockLocalLLMClient`.

---

## 18. Acceptance Criteria

This feature is complete when:

- User can open a Documentation Assistant screen.
- User can ask a question about local documentation.
- App retrieves relevant local snippets.
- App builds a grounded prompt.
- App can answer using a mock local LLM client.
- App has an Ollama client implementation ready for local calls.
- App can optionally use Ollama if enabled in composition root.
- Answer includes source documents.
- App shows honest fallback when documentation does not contain the answer.
- App does not call external AI providers.
- App does not expose Ollama publicly.
- Presentation uses Domain use cases only.
- SwiftUI views do not call Ollama directly.
- Tests do not require Ollama.
- Development notes are added.
- `docs/project-tree.md` is updated.

---

## 19. Do Not Implement Yet

Do not implement:

- Cloud AI providers
- Embeddings
- Vector database
- Real Confluence API integration
- Real Azure DevOps integration
- Real GitHub integration
- GitHub Actions integration
- Automatic document updates
- Agent tools
- Terminal command execution
- Filesystem access beyond existing local mock documents
- Authentication
- Permissions
- Production deployment

---

# Prompt for Codex

Use this prompt to ask Codex to add the feature.

---

## Feature — Local Ollama Documentation Assistant

Read `AI_Rules_ShellDoc.md` first.

Add a new feature to ShellDoc called **Local Ollama Documentation Assistant**.

This feature must allow the app to answer questions using the local documentation stored in the app.

Do not connect to external AI providers.

Do not call OpenAI, Claude, Gemini, Azure OpenAI, GitHub Copilot or any cloud AI service.

The assistant must be local-first and prepared to use Ollama running locally on the Mac.

Expected local Ollama endpoint:

```text
http://localhost:11434
```

Expected generate endpoint:

```text
POST http://localhost:11434/api/generate
```

Expected default model:

```text
qwen2.5-coder:7b
```

Also support configurable model names.

---

## Main Goal

Implement a RAG-ready assistant architecture:

```text
User question
    ↓
Retrieve local documentation snippets
    ↓
Build grounded prompt
    ↓
Generate answer with local LLM client
    ↓
Return answer with source documents
```

The assistant must answer only from retrieved documentation context.

If documentation does not contain enough information, the assistant must say so.

---

## Architecture

Use:

- SwiftUI
- MVVM
- Clean Architecture
- Repository Pattern
- UseCases
- Dependency Injection
- Async/Await

Respect module boundaries:

```text
SD-Presentation → SD-Domain ← SD-Data
```

Presentation must never access Data directly.

Presentation must never call Ollama directly.

SwiftUI views must never build prompts.

SwiftUI views must never perform retrieval logic.

---

## Domain Layer

Create the necessary Domain entities:

```swift
DocumentationQuestion
DocumentationAnswer
DocumentationAnswerConfidence
DocumentationSnippet
DocumentationSource
AssistantMessage
AssistantMessageRole
```

Create repository protocols:

```swift
DocumentationRetrievalRepository
DocumentationAssistantRepository
```

Create use case protocols and implementations:

```swift
RetrieveDocumentationSnippetsUseCaseProtocol
RetrieveDocumentationSnippetsUseCase
AskDocumentationAssistantUseCaseProtocol
AskDocumentationAssistantUseCase
```

Create a local LLM abstraction:

```swift
public protocol LocalLLMClient {
    func generateAnswer(prompt: String) async throws -> String
}
```

Create a prompt builder abstraction:

```swift
public protocol DocumentationAssistantPromptBuilder {
    func buildPrompt(
        question: String,
        snippets: [DocumentationSnippet]
    ) -> String
}
```

---

## Retrieval Logic

Implement simple local retrieval for this MVP.

Search local documentation using:

- Title
- Summary
- Content
- Tags
- Module
- Team
- Owner
- Status

Use simple scoring:

```text
Exact title match: +50
Partial title match: +35
Tag match: +30
Module match: +20
Summary match: +15
Content match: +10
Owner/team match: +5
```

Return top 3 to 5 snippets.

Do not implement embeddings yet.

---

## Prompt Builder

Create a strict prompt builder using this template:

```text
You are ShellDoc Assistant.

You answer questions only using the provided documentation context.

Rules:
- Use only the context below.
- If the answer is not present in the context, say the documentation does not contain enough information.
- Do not invent owners, tickets, commits, release notes, QA steps, APIs, business rules, or implementation details.
- Mention the source document title when useful.
- Keep the answer clear, technical, and concise.
- If the user asks for an update, suggest a draft change but do not modify the document automatically.

Documentation Context:
{{retrieved_context}}

User Question:
{{question}}

Answer:
```

---

## Data Layer

Create the following implementations in `SD-Data`:

```swift
LocalDocumentationRetrievalRepository
OllamaLocalLLMClient
OllamaDocumentationAssistantRepository
MockLocalLLMClient
MockDocumentationAssistantRepository
```

`OllamaLocalLLMClient` should call:

```text
POST http://localhost:11434/api/generate
```

Payload:

```json
{
  "model": "qwen2.5-coder:7b",
  "prompt": "...",
  "stream": false
}
```

Handle:

- Ollama not running
- Model not installed
- Timeout
- Empty response
- Invalid response
- Local connection failure

Unit tests must not require Ollama to be running.

Use `MockLocalLLMClient` for tests and previews.

---

## Presentation Layer

Create:

```swift
DocumentationAssistantView
DocumentationAssistantViewModel
```

The UI should include:

- Chat-style message list
- User question input
- Send button
- Loading state
- Error state
- Empty state
- Clear conversation action
- Source document references
- Local model status indicator

The answer must show the source documents used.

The assistant screen should be reachable from the existing ShellDoc navigation if navigation already exists.

Do not redesign unrelated screens.

---

## Design System

Create reusable components if missing:

```swift
SDAssistantMessageBubble
SDAssistantInputBar
SDAssistantSourceChip
SDAssistantSourcesPanel
SDLocalModelStatusBadge
SDTypingIndicator
```

Keep these generic and reusable.

Do not put business logic in Design System components.

---

## Behavior Rules

The assistant must:

- Answer only using retrieved documentation snippets.
- Show source documents.
- Say when documentation is insufficient.
- Avoid hallucinating undocumented processes.
- Avoid making unsupported claims.
- Suggest updates only as drafts.
- Never automatically modify documents.

The assistant must not:

- Call external AI APIs.
- Browse the internet.
- Execute commands.
- Access the filesystem directly.
- Save generated updates automatically.
- Store secrets.

---

## Tests

Add tests for:

- Retrieval scoring
- Snippet ranking
- Empty retrieval fallback
- Prompt builder output
- Ask assistant use case with `MockLocalLLMClient`
- Error handling for unavailable local LLM client if practical

Tests must not require Ollama to be installed or running.

---

## Acceptance Criteria

The feature is complete when:

- The app has a Documentation Assistant screen.
- The user can ask a question.
- The app retrieves relevant local documentation snippets.
- The app builds a strict grounded prompt.
- The app can generate an answer using a mock local LLM client.
- The app includes an Ollama local client implementation.
- The model name is configurable.
- The answer includes source documents.
- The app shows a clear fallback when no documentation supports the answer.
- The assistant does not call external AI providers.
- The assistant does not modify documentation automatically.
- Presentation uses Domain use cases only.
- SwiftUI views do not call Ollama directly.
- Unit tests do not depend on Ollama.
- Development notes are added.
- `docs/project-tree.md` is updated.

---

## Development Notes

After implementation, update development notes explaining:

- Local Ollama integration architecture
- RAG retrieval strategy
- Prompt builder strategy
- Mock vs Ollama LLM clients
- Privacy boundaries
- Known limitations
- Suggested next steps

---

## Suggested Next Steps

After this feature is complete, continue with:

```text
1. Improve retrieval ranking
2. Add document section chunking
3. Add source navigation from answer to document
4. Add suggested update draft flow
5. Add optional local embeddings later
6. Add model settings screen
```
