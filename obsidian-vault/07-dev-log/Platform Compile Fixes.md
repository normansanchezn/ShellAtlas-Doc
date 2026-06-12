---
title: "Dev Log - Platform Compile Fixes"
type: "dev-log"
created: 2026-06-12
updated: 2026-06-12
tags:
  - shelldoc
  - dev-log
  - ios
  - web
  - build
---

# Dev Log - Platform Compile Fixes

## What changed

- Added the missing `actual` implementations for `resizeHorizontalPointer()` on iOS and wasmJs.
- Reworked web config parsing to avoid non-portable JS interop in `WebAppConfig`.
- Kept the safe area fix in the root app shell so inset handling stays centralized.
- Fixed the iOS host project so Xcode can build and launch the simulator target again.
- Added `CADisableMinimumFrameDurationOnPhone` to the iOS `Info.plist` so Compose's plist sanity check no longer crashes on launch.

## Files created

- `feature/documents/src/iosMain/kotlin/com/shelldocs/feature/documents/ui/PointerModifiers.ios.kt`
- `feature/documents/src/wasmJsMain/kotlin/com/shelldocs/feature/documents/ui/PointerModifiers.wasmJs.kt`
- `obsidian-vault/07-dev-log/Platform Compile Fixes.md`

## Files modified

- `composeApp/src/commonMain/kotlin/com/shelldocs/app/App.kt`
- `composeApp/src/wasmJsMain/kotlin/com/shelldocs/app/WebAppConfig.kt`
- `iosApp/iosApp.xcodeproj/project.pbxproj`
- `iosApp/iosApp/Info.plist`
- `iosApp/iosApp/Preview Content/Contents.json`
- `obsidian-vault/02-architecture/KMM Architecture Overview.md`

## Decisions made

- Keep the resize cursor as a desktop-only affordance.
- Use a portable query-string parser on web instead of leaning on browser-specific helpers that complicate Kotlin/Wasm compilation.

## Issues found

- The feature-specific pointer modifier had only Android and desktop actuals, which broke iOS and wasmJs compilation.
- The iOS host project pointed at a missing `Config.xcconfig` path and a missing Preview Content folder, which prevented Xcode from testing the app.
- The iOS plist was missing the high-refresh-rate performance key required by ComposeUIViewController's sanity check.

## Tests added

- Compile verification for `:composeApp:compileKotlinIosSimulatorArm64` and `:composeApp:compileKotlinWasmJs`.
- Xcode simulator build verification for `iosApp` succeeded on the iPhone 17 simulator target.

## Next steps

- Rebuild and relaunch iOS to confirm the launch-time sanity check no longer fires.
