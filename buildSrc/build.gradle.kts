plugins {
    `kotlin-dsl`
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
}
buildscript {
    dependencyLocking {
        lockAllConfigurations()
    }
}
dependencyLocking {
    lockAllConfigurations()
}

repositories {
    mavenCentral()
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

ktlint {
    version.set("0.40.0")
    enableExperimentalRules.set(true)
    filter {
        exclude {
            it.file in fileTree(buildDir)
        }
    }
}

dependencies {
    // Workaround for https://github.com/JLLeitschuh/ktlint-gradle/issues/239
    runtimeOnly(embeddedKotlin("gradle-plugin"))
}
