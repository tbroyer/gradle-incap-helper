plugins {
    id("local.kotlin-base")
    `embedded-kotlin`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation(gradleTestKit())
}

evaluationDependsOn(":lib")
evaluationDependsOn(":processor")

publishing {
    repositories {
        maven(url = "$buildDir/repository") {
            name = "test"
        }
    }
    publications {
        create<MavenPublication>("lib") {
            from(project(":lib").components["java"])
            groupId = project(":lib").group.toString()
            artifactId = project(":lib").base.archivesBaseName
            version = project(":lib").version.toString()
        }
        create<MavenPublication>("processor") {
            from(project(":processor").components["java"])
            groupId = project(":processor").group.toString()
            artifactId = project(":processor").base.archivesBaseName
            version = project(":processor").version.toString()
        }
    }
}

tasks {
    test {
        inputs.files(project(":lib").tasks.named("jar"), project(":processor").tasks.named("jar"))

        systemProperty("version", rootProject.version.toString())

        dependsOn("publishLibPublicationToTestRepository", "publishProcessorPublicationToTestRepository")
    }
}
