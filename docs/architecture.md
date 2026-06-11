# ShellDocs — Architecture

## Layering (per the AUTH reference example)

```
┌────────────────────────────────────────────────────────────┐
│ ui/ (Compose Multiplatform)                                │
│   SignInScreen ── collects StateFlow, dispatches intents   │
├────────────────────────────────────────────────────────────┤
│ presentation/ (compose-free MVI)                           │
│   AuthIntent → AuthViewModel.handleIntent → AuthState      │
│                               └→ AuthEffect (one-shot)     │
├────────────────────────────────────────────────────────────┤
│ core/domain                                                │
│   SignInUseCase (validation + orchestration)               │
│   AuthRepository (interface)                               │
├────────────────────────────────────────────────────────────┤
│ core/data                                                  │
│   SupabaseAuthRepository → SupabaseAuthApi (GoTrue, Ktor)  │
│   DemoAuthRepository (demo mode)                           │
└────────────────────────────────────────────────────────────┘
```

Dependency rule: source code dependencies only point inwards
(ui → presentation → domain ← data). The domain layer has no Compose,
Ktor or Supabase imports.

## MVI contract (core/common)

- `MviIntent` — the only entry point to mutate state.
- `MviState` — immutable data-class snapshots.
- `MviEffect` — one-shot signals (navigation, notices), never replayed.
- `MviViewModel` — owns a `CoroutineScope` over an injected
  `DispatcherProvider`, so tests run on `StandardTestDispatcher` with
  virtual time.

## AI assistant pipeline

```
question
  └→ DetectAssistantIntentUseCase (QUESTION | EXPLAIN_FLOW | IMPROVE_DOCUMENT | SUMMARIZE)
  └→ AssistantCacheRepository.lookup (assistant_intelligence)
  └→ RetrieveGroundingDocumentsUseCase (keyword retrieval: title 3x, tags 2x, body 1x)
  └→ AssistantEngine.answer(question, intent, scored grounding)
        ├─ OllamaAssistantEngine   (strictly grounded prompt; optional)
        └─ GroundedAssistantEngine (deterministic templates; always available)
  └→ cache.save → AssistantAnswer { markdown, confidence, sources(relevance %) }
```

Key behaviors:

- **Knows when not to improve**: `ShouldImproveDocumentUseCase` runs the
  deterministic health audit (`EvaluateDocumentHealthUseCase`: staleness,
  status, thin content, TODO markers, missing summary). Healthy documents
  get an explicit "leave it alone" verdict with the rationale.
- **Explains flows**: `EXPLAIN_FLOW` rebuilds the document's heading/list
  structure into an ordered, step-by-step walkthrough.
- **Confidence** is derived from retrieval scores
  (`AnswerConfidence.fromRetrievalScore`), never invented.

## Role enforcement (defense in depth)

1. **UI** hides actions the role lacks (`DocumentsState.canEdit`, etc.).
2. **Domain** use cases check `RolePermissions` and fail with
   `AppError.Unauthorized` (`AssignRoleUseCase`, `PublishDocumentUseCase`,
   `DeleteDocumentUseCase`, `SyncSourceUseCase`, ...).
3. **Database** RLS policies gate writes by `public.role_of(auth.uid())`.

## Testing strategy

- Domain/data/presentation are pure Kotlin → tested with `kotlin.test`
  + `kotlinx-coroutines-test`; HTTP clients tested with Ktor `MockEngine`.
- Fixtures live in `core/domain` commonTest (`DocumentFixtures`,
  `FakeDocumentRepository`, `FixedTimeProvider`).
- Compose `ui/` packages are thin render layers over tested state.
