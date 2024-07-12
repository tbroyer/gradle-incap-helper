package local

plugins {
    id("com.diffplug.spotless")
}

spotless {
    kotlinGradle {
        ktlint(
            project
                .the<VersionCatalogsExtension>()
                .named("libs")
                .findVersion("ktlint")
                .orElseThrow()
                .requiredVersion,
        )
    }
}
