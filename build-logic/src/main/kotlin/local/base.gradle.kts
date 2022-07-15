package local

plugins {
    id("com.diffplug.spotless")
}

dependencyLocking {
    lockAllConfigurations()
    lockMode.set(LockMode.STRICT)
}

spotless {
    kotlinGradle {
        ktlint(project.the<VersionCatalogsExtension>().named("libs").findVersion("ktlint").orElseThrow().requiredVersion)
    }
}
