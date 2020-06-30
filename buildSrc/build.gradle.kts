plugins {
    `kotlin-dsl`
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
}

repositories {
    mavenCentral()
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

ktlint {
    version.set("0.37.2")
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
