plugins {
    `kotlin-dsl`
    alias(libs.plugins.spotless)
}
buildscript {
    dependencyLocking {
        lockAllConfigurations()
    }
}
dependencyLocking {
    lockAllConfigurations()
}

spotless {
    kotlinGradle {
        target("*.gradle.kts", "src/main/kotlin/**/*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
    }
}

dependencies {
    implementation(plugin(libs.plugins.spotless))
    implementation(plugin(libs.plugins.errorprone))
    implementation(plugin(libs.plugins.nullaway))
}

// Simplify declaration of dependencies to plugins
// https://github.com/gradle/gradle/issues/17963
// https://docs.gradle.org/current/userguide/plugins.html#sec:plugin_markers
fun plugin(plugin: Provider<PluginDependency>) = plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
