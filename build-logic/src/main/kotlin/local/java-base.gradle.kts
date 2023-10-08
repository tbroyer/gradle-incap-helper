package local

plugins {
    id("local.base")
    `java-base`
}
project.findProperty("test.java-toolchain")?.also { testJavaToolchain ->
    tasks.withType<Test>().configureEach {
        javaLauncher =
            project.javaToolchains.launcherFor {
                languageVersion = JavaLanguageVersion.of(testJavaToolchain.toString())
            }
    }
}

spotless {
    java {
        googleJavaFormat(
            project.the<VersionCatalogsExtension>().named("libs").findVersion("googleJavaFormat").orElseThrow().requiredVersion,
        )
        licenseHeaderFile(rootProject.file("LICENSE.header"))
    }
}
