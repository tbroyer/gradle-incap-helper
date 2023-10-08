plugins {
    id("local.kotlin-base")
    `embedded-kotlin`
}

// Use the lowest toolchain to compile the code, such that produced bytecode
// is compatible with the target test toolchain.
project.findProperty("test.java-toolchain")?.also { testJavaToolchain ->
    val testVersion = JavaLanguageVersion.of(testJavaToolchain.toString())
    if (testVersion < JavaLanguageVersion.of(JavaVersion.current().toString())) {
        java {
            toolchain {
                languageVersion = testVersion
            }
        }
    }
}

// XXX: separate "dependency bucket" from resolvable configuration?
val localMavenRepositories by configurations.creating {
    isCanBeDeclared = true
    isCanBeConsumed = false
    isCanBeResolved = true
    // Same attributes as in local.maven-publish convention plugin
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named("maven-repository"))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
    }
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(gradleTestKit())

    localMavenRepositories(projects.lib)
    localMavenRepositories(projects.processor)
}

tasks {
    test {
        inputs.files(
            localMavenRepositories.asFileTree.matching {
                exclude("**/maven-metadata.*")
            },
        )
            .withPropertyName("testRepositories")
            .withPathSensitivity(PathSensitivity.RELATIVE)

        systemProperty("version", rootProject.version.toString())
        // systemProperty doesn't support providers, so fake it with CommandLineArgumentProvider
        jvmArgumentProviders.add(
            CommandLineArgumentProvider {
                listOf("-DtestRepositories=${localMavenRepositories.joinToString(File.pathSeparator) { project.relativePath(it) }}")
            },
        )
    }
}
