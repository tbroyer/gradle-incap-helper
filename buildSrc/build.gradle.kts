plugins {
    `kotlin-dsl`
    id("com.diffplug.spotless") version "6.3.0"
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

spotless {
    kotlinGradle {
        ktlint("0.40.0")
    }
}
