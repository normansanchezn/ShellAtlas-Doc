---
title: "User Feedback Flow"
type: "diagram"
status: "active"
created: 2026-06-11
updated: 2026-06-11
tags:
  - shelldoc
  - diagram
  - feedback
---

# User Feedback Flow

```mermaid
flowchart TD
    A["User action"] --> B["ViewModel intent handler"]
    B --> C["UseCase execution"]
    C --> D{"Result"}
    D -->|Success| E["Clear loading state"]
    D -->|Failure| F["Map AppError to ErrorDialogState"]
    B --> G["Set blocking progress state when needed"]
    G --> H["ShellLoadingOverlay as modal dialog"]
    F --> I["ShellErrorDialog"]
    E --> J["Updated UI state"]
    H --> J
    I --> J
    J --> K["Shared placeholder frame for empty/loading/error states"]
```
