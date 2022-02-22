package local

plugins {
    id("local.base")
    `java-base`
}
project.findProperty("test.java-toolchain")?.also { testJavaToolchain ->
    tasks.withType<Test>().configureEach {
        javaLauncher.set(
            project.javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(testJavaToolchain.toString()))
            }
        )
    }
}

spotless {
    java {
        googleJavaFormat("1.14.0")
        licenseHeaderFile(rootProject.file("LICENSE.header"))
    }
}
