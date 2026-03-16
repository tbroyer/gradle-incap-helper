package local

plugins {
    id("local.java-library")
    id("com.vanniktech.maven.publish")
    signing
}

group = "net.ltgt.gradle.incap"

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    pom {
        name = provider { "${project.group}:${project.name}" }
        description = provider { project.description ?: name.get() }
        url = "https://github.com/tbroyer/gradle-incap-helper"
        developers {
            developer {
                name = "Thomas Broyer"
                email = "t.broyer@ltgt.net"
            }
        }
        scm {
            connection = "https://github.com/tbroyer/gradle-incap-helper.git"
            developerConnection = "scm:git:ssh://github.com:tbroyer/gradle-incap-helper.git"
            url = "https://github.com/tbroyer/gradle-incap-helper"
        }
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
    }
}

publishing.publications.withType<MavenPublication> {
    versionMapping {
        usage("java-api") {
            fromResolutionOf("runtimeClasspath")
        }
        usage("java-runtime") {
            fromResolutionResult()
        }
    }
}

signing {
    useGpgCmd()
}

//
// For integration tests
//
// Inspired by https://github.com/sigstore/sigstore-java/pull/264/files

// Use a dedicated publication so we can skip signing it
// name must already be capitalized for computing task name below
val localPublication = publishing.publications.create<MavenPublication>("Local") {
    from(components["java"])
}

val localRepoDir = layout.buildDirectory.dir("local-maven-repo")

val localRepository =
    publishing.repositories.maven {
        name = "Local" // must already be capitalized for computing task name below
        url = uri(localRepoDir)
    }

tasks {
    val cleanLocalRepository by registering(Delete::class) {
        delete(localRepoDir)
    }
    withType<PublishToMavenRepository>().configureEach {
        if (repository == localRepository) {
            val predicate = provider { publication == localPublication }
            onlyIf { predicate.get() }
            dependsOn(cleanLocalRepository)
        }
    }
    // Disable signing so it passes on CI
    named("sign${localPublication.name}Publication") {
        enabled = false
    }
}

configurations {
    consumable("localRepoElements") {
        description = "Shares local maven repository directory that contains the artifacts produced by the current project"
        attributes {
            attribute(Category.CATEGORY_ATTRIBUTE, objects.named("maven-repository"))
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
        outgoing {
            artifact(localRepoDir) {
                builtBy(tasks.named("publish${localPublication.name}PublicationTo${localRepository.name}Repository"))
            }
        }
    }
}
