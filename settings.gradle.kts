pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "gradle-incap-helper"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":incap", ":incap-processor", ":integTest")

project(":incap").projectDir = file("lib")
project(":incap-processor").projectDir = file("processor")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
