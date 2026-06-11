---
title: "Architecture Diagram"
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
  - architecture
---

# Architecture Diagram

```mermaid
flowchart TD
    A["Compose / SwiftUI Host"] --> B["Feature UI"]
    B --> C["Presentation (MVI ViewModels)"]
    C --> D["Domain UseCases"]
    D --> E["Repository Interfaces"]
    F["Data Repositories"] --> E
    F --> G["DemoSeed / Remote Adapters"]
```
