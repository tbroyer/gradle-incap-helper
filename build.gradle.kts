import nl.javadude.gradle.plugins.license.LicenseExtension
import java.time.Year

plugins {
    base
    id("com.github.sherter.google-java-format") version "0.7.1"

    id("com.github.hierynomus.license") version "0.14.0" apply false
    // Used by "local.java-library"
    id("net.ltgt.errorprone-javacplugin") version "0.5" apply false
}

version = VersionFromGit(project, "HEAD-SNAPSHOT")

repositories {
    mavenCentral()
    jcenter() // for ktlint's kolor dependency
}

googleJavaFormat {
    toolVersion = "1.6"
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
    ktlint("com.github.shyiko:ktlint:0.27.0")
}

tasks {
    val verifyKtlint by registering(JavaExec::class) {
        description = "Check Kotlin code style."
        classpath = ktlint
        main = "com.github.shyiko.ktlint.Main"
        args("**/*.gradle.kts", "**/*.kt")
    }

    "check" {
        dependsOn(verifyKtlint)
    }

    register("ktlint", JavaExec::class) {
        description = "Fix Kotlin code style violations."
        classpath = ktlint
        main = "com.github.shyiko.ktlint.Main"
        args("-F", "**/*.gradle.kts", "**/*.kt")
    }
}

inline fun Project.license(noinline configuration: LicenseExtension.() -> Unit) = configure(configuration)
