plugins {
    id("local.kotlin-base")
    `embedded-kotlin`
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

    localMavenRepositories(projects.incap)
    localMavenRepositories(projects.incapProcessor)
}

tasks {
    test {
        val testJavaToolchain = project.findProperty("test.java-toolchain")?.toString()
        testJavaToolchain?.also {
            val metadata =
                project.javaToolchains
                    .launcherFor {
                        languageVersion.set(JavaLanguageVersion.of(testJavaToolchain))
                    }.get()
                    .metadata
            systemProperty("test.java-toolchain", testJavaToolchain)
            systemProperty("test.java-home", metadata.installationPath.asFile.canonicalPath)
        }

        systemProperty("version", rootProject.version.toString())

        jvmArgumentProviders.add(TestRepositories(localMavenRepositories))
    }
}

class TestRepositories(
    private val testRepositories: FileCollection,
) : CommandLineArgumentProvider,
    Named {
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    fun getTestRepositories() =
        testRepositories.asFileTree.matching {
            exclude("**/maven-metadata.*")
        }

    @Internal
    override fun getName() = "testRepositories"

    override fun asArguments() = listOf("-DtestRepositories=${testRepositories.joinToString(File.pathSeparator)}")
}
