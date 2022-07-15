package local

plugins {
    id("local.java-base")
}

spotless {
    kotlin {
        ktlint(project.the<VersionCatalogsExtension>().named("libs").findVersion("ktlint").orElseThrow().requiredVersion)
        licenseHeaderFile(rootProject.file("LICENSE.header"))
    }
}
