import java.time.Year
import nl.javadude.gradle.plugins.license.LicenseExtension

plugins {
    base
    id("com.github.sherter.google-java-format") version "0.8"

    id("com.github.hierynomus.license") version "0.15.0" apply false
    // Used by "local.java-library"
    id("net.ltgt.errorprone") version "1.1.1" apply false
}

version = VersionFromGit(project, "HEAD-SNAPSHOT")

repositories {
    mavenCentral()
    jcenter() // for ktlint's kolor dependency
}

googleJavaFormat {
    toolVersion = "1.7"
}

subprojects {
    apply(plugin = "com.github.hierynomus.license")

    license {
        header = rootProject.file("LICENSE.header")
        encoding = "UTF-8"
        skipExistingHeaders = true
        mapping("java", "SLASHSTAR_STYLE")
        exclude("**/META-INF/**")

        (this as ExtensionAware).extra["year"] = Year.now()
        (this as ExtensionAware).extra["name"] = "Thomas Broyer"
    }
}

val ktlint by configurations.creating

dependencies {
    ktlint("com.pinterest:ktlint:0.36.0")
}

tasks {
    val verifyKtlint by registering(JavaExec::class) {
        description = "Check Kotlin code style."
        classpath = ktlint
        main = "com.pinterest.ktlint.Main"
        args("**/*.gradle.kts", "**/*.kt", "!**/build/**")
    }

    check {
        dependsOn(verifyKtlint)
    }

    register("ktlint", JavaExec::class) {
        description = "Fix Kotlin code style violations."
        classpath = ktlint
        main = "com.pinterest.ktlint.Main"
        args("-F", "**/*.gradle.kts", "**/*.kt", "!**/build/**")
    }
}

inline fun Project.license(noinline configuration: LicenseExtension.() -> Unit) = configure(configuration)
