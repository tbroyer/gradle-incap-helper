plugins {
    base
    id("com.diffplug.spotless") version "6.3.0"

    // Used by "local.java-library"
    id("net.ltgt.errorprone") version "1.3.0" apply false
    id("net.ltgt.nullaway") version "1.0.2" apply false
}

version = VersionFromGit(project, "HEAD-SNAPSHOT")

allprojects {
    dependencyLocking {
        lockAllConfigurations()
        lockMode.set(LockMode.STRICT)
    }
}
tasks {
    register("allDependencies") {
        dependsOn(getTasksByName("dependencies", true))
    }
}

project.findProperty("test.java-toolchain")?.also { testJavaToolchain ->
    subprojects {
        pluginManager.withPlugin("java-base") {
            tasks.withType<Test>().configureEach {
                javaLauncher.set(
                    project.the<JavaToolchainService>().launcherFor {
                        languageVersion.set(JavaLanguageVersion.of(testJavaToolchain.toString()))
                    }
                )
            }
        }
    }
}

repositories {
    mavenCentral()
}

allprojects {
    apply(plugin = "com.diffplug.spotless")

    spotless {
        kotlinGradle {
            ktlint("0.40.0")
        }
        pluginManager.withPlugin("java-base") {
            java {
                googleJavaFormat("1.7")
                licenseHeaderFile(rootProject.file("LICENSE.header"))
            }
        }
        pluginManager.withPlugin("java-base") {
            kotlin {
                ktlint("0.40.0")
                licenseHeaderFile(rootProject.file("LICENSE.header"))
            }
        }
    }
}
