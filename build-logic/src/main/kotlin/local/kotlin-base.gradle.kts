package local

plugins {
    id("local.java-base")
}

spotless {
    kotlin {
        ktlint(
            versionCatalogs
                .named("libs")
                .findVersion("ktlint")
                .orElseThrow()
                .requiredVersion,
        )
        licenseHeaderFile(rootProject.file("LICENSE.header"))
    }
}
