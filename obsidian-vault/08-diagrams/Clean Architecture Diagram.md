---
title: "Clean Architecture Diagram"
type: "diagram"
status: "active"
platform: "Android/iOS/Desktop/Web"
area: "ShellDoc"
owner: "Product Engineering"
created: 2026-06-11
updated: 2026-06-11
tags:
  - shelldoc
  - diagram
  - clean-architecture
---

# Clean Architecture Diagram

```mermaid
flowchart LR
    UI["feature/*/ui"] --> PRESENTATION["feature/*/presentation"]
    PRESENTATION --> DOMAIN["core/domain"]
    DATA["core/data"] --> DOMAIN
    HOSTS["composeApp + iosApp"] --> UI
```
