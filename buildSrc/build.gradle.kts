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
    gradlePluginPortal()
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

spotless {
    kotlinGradle {
        ktlint("0.40.0")
    }
}

dependencies {
    implementation(plugin(id = "com.diffplug.spotless", version = "6.3.0"))
    implementation(plugin(id = "net.ltgt.errorprone", version = "1.3.0"))
    implementation(plugin(id = "net.ltgt.nullaway", version = "1.0.2"))
}

// https://docs.gradle.org/current/userguide/plugins.html#sec:plugin_markers
fun plugin(id: String, version: String) = "$id:$id.gradle.plugin:$version"
