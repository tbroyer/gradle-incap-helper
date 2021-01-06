import nl.javadude.gradle.plugins.license.LicenseExtension
import java.time.Year

plugins {
    base
    id("com.github.sherter.google-java-format") version "0.9"
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"

    id("com.github.hierynomus.license") version "0.15.0" apply false
    // Used by "local.java-library"
    id("net.ltgt.errorprone") version "1.2.1" apply false
}

version = VersionFromGit(project, "HEAD-SNAPSHOT")

repositories {
    mavenCentral()
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

allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    ktlint {
        version.set("0.37.2")
        enableExperimentalRules.set(true)
    }
}

inline fun Project.license(noinline configuration: LicenseExtension.() -> Unit) = configure(configuration)
