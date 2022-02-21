rootProject.name = "gradle-incap-helper"

include(":lib", ":processor", ":integTest")

gradle.beforeProject {
    buildscript {
        dependencyLocking {
            lockAllConfigurations()
            lockMode.set(LockMode.STRICT)
        }
    }
}
