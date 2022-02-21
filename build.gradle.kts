plugins {
    id("local.base")
    base
}

version = VersionFromGit(project, "HEAD-SNAPSHOT")

tasks {
    register("allDependencies") {
        dependsOn(getTasksByName("dependencies", true))
    }
}

repositories {
    mavenCentral()
}
