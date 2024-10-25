pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "gradle-incap-helper"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":lib", ":processor", ":integTest")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
