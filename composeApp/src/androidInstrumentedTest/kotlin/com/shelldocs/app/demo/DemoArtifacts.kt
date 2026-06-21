package com.shelldocs.app.demo

import android.app.Instrumentation
import android.os.Environment
import android.util.Log
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val DemoArtifactsTag = "ShellAtlasDemoArtifacts"

class DemoArtifacts(private val instrumentation: Instrumentation) {

    private val arguments = InstrumentationRegistry.getArguments()
    private val recordDemoVideo = arguments.getString("recordDemoVideo")?.toBooleanStrictOrNull() ?: false
    private val captureDemoSnapshots = arguments.getString("captureDemoSnapshots")?.toBooleanStrictOrNull() ?: true

    private var currentVideoPath: String? = null

    fun startFlowRecording(flowName: String) {
        if (!recordDemoVideo) return

        val safeName = safeArtifactName(flowName)
        currentVideoPath = "/sdcard/Movies/ShellAtlasDemo/${timestamp()}-$safeName.mp4"
        shell("mkdir -p /sdcard/Movies/ShellAtlasDemo")
        shell(
            "sh -c 'screenrecord --bit-rate 8000000 --time-limit 180 \"$currentVideoPath\" >/dev/null 2>&1 &'",
        )
        Log.i(DemoArtifactsTag, "Started demo recording: $currentVideoPath")
        Thread.sleep(1_000L)
    }

    fun stopFlowRecording() {
        val videoPath = currentVideoPath ?: return
        shell("sh -c 'killall -INT screenrecord >/dev/null 2>&1 || pkill -INT screenrecord >/dev/null 2>&1 || true'")
        Thread.sleep(1_000L)
        Log.i(DemoArtifactsTag, "Stopped demo recording: $videoPath")
        currentVideoPath = null
    }

    fun captureRootSnapshot(
        composeRule: AndroidComposeTestRule<*, *>,
        snapshotName: String,
    ): File? {
        if (!captureDemoSnapshots) return null
        @Suppress("UNUSED_PARAMETER")
        val ignoredRule = composeRule

        val outputDir = File(
            instrumentation.targetContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "shellatlas-demo",
        )
        outputDir.mkdirs()

        val outputFile = File(outputDir, "${timestamp()}-${safeArtifactName(snapshotName)}.png")
        shell("screencap -p \"${outputFile.absolutePath}\"")
        val capturedFile = outputFile.takeIf { it.exists() }
        if (capturedFile != null) {
            Log.i(DemoArtifactsTag, "Saved demo snapshot: ${capturedFile.absolutePath}")
        } else {
            Log.w(DemoArtifactsTag, "Device screencap did not produce a snapshot file")
        }
        return capturedFile
    }

    private fun shell(command: String) {
        instrumentation.uiAutomation.executeShellCommand(command).close()
    }

    private fun timestamp(): String =
        SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())

    private fun safeArtifactName(value: String): String =
        value.lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
}
