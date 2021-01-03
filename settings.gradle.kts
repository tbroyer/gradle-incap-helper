rootProject.name = "gradle-incap-helper"

include(":lib", ":processor", ":integTest")

enableFeaturePreview("ONE_LOCKFILE_PER_PROJECT")
gradle.beforeProject {
    buildscript {
        dependencyLocking {
            lockAllConfigurations()
            lockMode.set(LockMode.STRICT)
        }
    }
}
