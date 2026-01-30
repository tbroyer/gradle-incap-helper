package local

import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("local.java-base")
    `java-library`
    id("net.ltgt.errorprone")
    id("net.ltgt.nullaway")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 8
    // release=8 is deprecated with JDK 21
    options.compilerArgs.add("-Xlint:all,-options")
}
project.findProperty("test.java-toolchain")?.also { testJavaToolchain ->
    tasks.withType<Test>().configureEach {
        javaLauncher =
            project.javaToolchains.launcherFor {
                languageVersion = JavaLanguageVersion.of(testJavaToolchain.toString())
            }
    }
}

dependencies {
    errorprone(
        project
            .the<VersionCatalogsExtension>()
            .named("libs")
            .findBundle("errorprone")
            .orElseThrow(),
    )
}

nullaway {
    jspecifyMode = true
}
tasks {
    withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(listOf("-Werror", "-Xlint:all,-processing"))
        options.errorprone {
            // XXX: text blocks aren't supported in --release 8
            // https://github.com/google/error-prone/issues/4931
            disable("StringConcatToTextBlock")
        }
    }
    javadoc {
        (options as CoreJavadocOptions).addBooleanOption("Xdoclint:all,-missing", true)
        if (JavaVersion.current().isJava9Compatible) {
            (options as CoreJavadocOptions).addBooleanOption("html5", true)
        }
    }
}
