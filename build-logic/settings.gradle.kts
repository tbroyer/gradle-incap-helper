pluginManagement {
    repositories {
        // gradlePluginPortal redirects to JCenter which isn't reliable,
        // prefer Central to JCenter (for the same dependencies)
        // cf. https://github.com/gradle/gradle/issues/15406
        mavenCentral()
        gradlePluginPortal()
    }
}
buildscript {
    dependencyLocking {
        lockAllConfigurations()
        lockMode.set(LockMode.STRICT)
    }
}

dependencyResolutionManagement {
    repositories {
        // gradlePluginPortal redirects to JCenter which isn't reliable,
        // prefer Central to JCenter (for the same dependencies)
        // cf. https://github.com/gradle/gradle/issues/15406
        mavenCentral()
        gradlePluginPortal()
    }
}
