package local

plugins {
    id("local.java-base")
    `java-library`
    id("net.ltgt.errorprone")
    id("net.ltgt.nullaway")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 8
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
    errorprone(project.the<VersionCatalogsExtension>().named("libs").findBundle("errorprone").orElseThrow())
}

tasks {
    withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(listOf("-Werror", "-Xlint:all,-processing"))
    }
    javadoc {
        (options as CoreJavadocOptions).addBooleanOption("Xdoclint:all,-missing", true)
        if (JavaVersion.current().isJava9Compatible) {
            (options as CoreJavadocOptions).addBooleanOption("html5", true)
        }
    }
}
