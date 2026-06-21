package com.shelldocs.feature.auth.ui

internal actual fun isInstrumentedUiTestRuntime(): Boolean =
    runCatching {
        val registryClass = Class.forName("androidx.test.platform.app.InstrumentationRegistry")
        val getArguments = registryClass.getMethod("getArguments")
        val bundle = getArguments.invoke(null) as? android.os.Bundle
        bundle?.containsKey("class") == true || bundle?.containsKey("recordDemoVideo") == true
    }.getOrDefault(false)
