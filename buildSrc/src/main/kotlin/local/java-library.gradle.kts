package local

plugins {
    `java-library`
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
if (JavaVersion.current().isJava9Compatible) {
    tasks.withType<JavaCompile> {
        options.compilerArgs.addAll(listOf("--release", java.targetCompatibility.majorVersion))
    }
}

if (JavaVersion.current().isJava9Compatible) {
    apply(plugin = "net.ltgt.errorprone-javacplugin")
} else {
    apply(plugin = "net.ltgt.errorprone")
}

repositories {
    mavenCentral()
}

dependencies {
    "errorprone"("com.google.errorprone:error_prone_core:2.3.1")
}

val javadoc by tasks.getting(Javadoc::class) {
    (options as CoreJavadocOptions).addBooleanOption("Xdoclint:all,-missing", true)
}

inline val Project.java: JavaPluginConvention
    get() = the()
