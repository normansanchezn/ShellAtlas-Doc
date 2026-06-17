// The Kotlin/Wasm dev server defaults devServer.liveReload to true, which
// force-reloads the page whenever it sees the build output change — wiping
// any unsaved form input (e.g. sign-in email/password) even when the change
// was unrelated to the page itself (background daemon writes, IDE indexing).
// There's no toggle for this in the Kotlin Gradle DSL, so patch it here.
config.devServer = config.devServer || {};
config.devServer.liveReload = false;
