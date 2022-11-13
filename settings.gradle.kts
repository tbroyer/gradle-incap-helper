pluginManagement {
    repositories {
        // gradlePluginPortal redirects to JCenter which isn't reliable,
        // prefer Central to JCenter (for the same dependencies)
        // cf. https://github.com/gradle/gradle/issues/15406
        mavenCentral()
        gradlePluginPortal()
    }

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
