package local

plugins {
    id("com.diffplug.spotless")
}

spotless {
    kotlinGradle {
        ktlint(
            versionCatalogs
                .named("libs")
                .findVersion("ktlint")
                .orElseThrow()
                .requiredVersion,
        )
    }
}
