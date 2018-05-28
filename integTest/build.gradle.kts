plugins {
    `embedded-kotlin`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.12")
    testImplementation(gradleTestKit())

    testRuntimeOnly(configurations.embeddedKotlin)
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

val test by tasks.getting(Test::class) {
    systemProperty("version", rootProject.version)

    dependsOn("publishLibPublicationToTestRepository", "publishProcessorPublicationToTestRepository")
}
