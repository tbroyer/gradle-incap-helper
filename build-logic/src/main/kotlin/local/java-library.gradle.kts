package local

plugins {
    id("local.java-base")
    `java-library`
    id("net.ltgt.errorprone")
    id("net.ltgt.nullaway")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
}

dependencies {
    errorprone(project.the<VersionCatalogsExtension>().named("libs").findBundle("errorprone").orElseThrow())
    errorproneJavac("com.google.errorprone:javac:9+181-r4173-1")
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
