package local

plugins {
    `java-library`
    id("net.ltgt.errorprone-javacplugin")
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
if (JavaVersion.current().isJava9Compatible) {
    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(listOf("--release", java.targetCompatibility.majorVersion))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    "errorprone"("com.google.errorprone:error_prone_core:2.3.1")
    "errorproneJavac"("com.google.errorprone:javac:9+181-r4173-1")
}

tasks {
    "javadoc"(Javadoc::class) {
        (options as CoreJavadocOptions).addBooleanOption("Xdoclint:all,-missing", true)
    }
}

inline val Project.java: JavaPluginExtension
    get() = the()
