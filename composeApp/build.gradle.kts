import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.File

fun loadDotEnv(rootDir: File): Map<String, String> {
    val envFile = rootDir.resolve(".env")
    if (!envFile.exists()) return emptyMap()

    return envFile.readLines()
        .map(String::trim)
        .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
        .associate { line ->
            val separator = line.indexOf('=')
            val key = line.substring(0, separator).trim()
            val value = line.substring(separator + 1).trim().removeSurrounding("\"")
            key to value
        }
}

val dotEnv = loadDotEnv(rootDir)

fun envOrDotEnv(name: String): String =
    providers.environmentVariable(name).orNull
        ?.takeIf { it.isNotBlank() }
        ?: dotEnv[name].orEmpty()

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(projects.core.common)
            implementation(projects.core.domain)
            implementation(projects.core.data)
            implementation(projects.core.designsystem)
            implementation(projects.feature.auth)
            implementation(projects.feature.assistant)
            implementation(projects.feature.documents)
            implementation(projects.feature.updates)
            implementation(projects.feature.dashboard)
            implementation(projects.feature.sources)
            implementation(projects.feature.settings)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.ktor.client.cio)
            }
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}

android {
    namespace = "com.shelldocs.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.shelldocs.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField("String", "SUPABASE_URL", "\"${envOrDotEnv("SHELLDOC_SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${envOrDotEnv("SHELLDOC_SUPABASE_ANON_KEY")}\"")
        buildConfigField("String", "API_BASE_URL", "\"${envOrDotEnv("SHELLDOC_API_BASE_URL")}\"")
        buildConfigField("String", "API_BEARER_TOKEN", "\"${envOrDotEnv("SHELLDOC_API_BEARER_TOKEN")}\"")
        buildConfigField("boolean", "USE_OLLAMA", envOrDotEnv("SHELLDOC_USE_OLLAMA").equals("true", ignoreCase = true).toString())
        buildConfigField("String", "OLLAMA_BASE_URL", "\"${envOrDotEnv("SHELLDOC_OLLAMA_BASE_URL").ifBlank { "http://10.0.2.2:11434" }}\"")
        buildConfigField("String", "OLLAMA_MODEL", "\"${envOrDotEnv("SHELLDOC_OLLAMA_MODEL").ifBlank { "llama3.1" }}\"")
    }
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

compose.desktop {
    application {
        mainClass = "com.shelldocs.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ShellDocs"
            packageVersion = "1.0.0"
        }
    }
}
