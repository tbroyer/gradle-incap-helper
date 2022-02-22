buildscript {
    dependencyLocking {
        lockAllConfigurations()
        lockMode.set(LockMode.STRICT)
    }
}
gradle.beforeProject {
    buildscript {
        dependencyLocking {
            lockAllConfigurations()
            lockMode.set(LockMode.STRICT)
        }
    }
}

rootProject.name = "gradle-incap-helper"

include(":lib", ":processor", ":integTest")
