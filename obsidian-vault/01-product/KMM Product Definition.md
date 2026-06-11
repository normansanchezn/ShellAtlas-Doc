---
title: "KMM Product Definition"
type: "product"
status: "active"
platform: "Android/iOS/Desktop/Web"
area: "ShellDoc"
owner: "Product Engineering"
created: 2026-06-11
updated: 2026-06-11
tags:
  - shelldoc
  - product
  - kmm
---

# KMM Product Definition

## Summary

ShellDoc is a multiplatform knowledge product for engineering documentation, grounded in deterministic demo data and prepared for future live integrations.

## Purpose

Keep a single product implementation in KMM while preserving useful recovered logic from prior Swift experiments.

## Expected Behavior

- Assistant-first navigation.
- Explorer/editor workflow for documents.
- Deterministic document health and updates triage.
- Mock sources and demo data for safe offline operation.

## Related Files

- `composeApp/`
- `core/`
- `feature/`
- `iosApp/`

## Development Notes

- KMM is the only product source of truth.
- Recovered Swift logic may be ported into KMM, but the Swift app tree itself is not retained.
