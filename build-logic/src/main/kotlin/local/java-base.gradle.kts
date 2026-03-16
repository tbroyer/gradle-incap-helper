package local

plugins {
    id("local.base")
    `java-base`
}

spotless {
    java {
        googleJavaFormat(
            versionCatalogs
                .named("libs")
                .findVersion("googleJavaFormat")
                .orElseThrow()
                .requiredVersion,
        )
        licenseHeaderFile(rootProject.file("LICENSE.header"))
    }
}
