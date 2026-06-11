rootProject.name = "ShellDocs"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":composeApp")
include(":core:common")
include(":core:domain")
include(":core:data")
include(":core:designsystem")
include(":feature:auth")
include(":feature:assistant")
include(":feature:documents")
include(":feature:updates")
include(":feature:dashboard")
include(":feature:sources")
include(":feature:settings")
