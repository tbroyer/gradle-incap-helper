plugins {
    id("local.kotlin-base")
    `embedded-kotlin`
    `maven-publish`
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(gradleTestKit())
}

evaluationDependsOn(projects.lib.dependencyProject.path)
evaluationDependsOn(projects.processor.dependencyProject.path)

publishing {
    repositories {
        maven(url = "$buildDir/repository") {
            name = "test"
        }
    }
    publications {
        create<MavenPublication>("lib") {
            from(projects.lib.dependencyProject.components["java"])
            groupId = projects.lib.group.toString()
            artifactId = projects.lib.dependencyProject.base.archivesName.get()
            version = projects.lib.version.toString()
        }
        create<MavenPublication>("processor") {
            from(projects.processor.dependencyProject.components["java"])
            groupId = projects.processor.group.toString()
            artifactId = projects.processor.dependencyProject.base.archivesName.get()
            version = projects.processor.version.toString()
        }
    }
}

tasks {
    test {
        inputs.files(
            project.projects.lib.dependencyProject.tasks.named("jar"),
            project.projects.processor.dependencyProject.tasks.named("jar")
        )

        systemProperty("version", rootProject.version.toString())

        dependsOn("publishLibPublicationToTestRepository", "publishProcessorPublicationToTestRepository")
    }
}
