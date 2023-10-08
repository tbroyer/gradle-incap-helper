package local

plugins {
    id("local.java-library")
    `maven-publish`
    signing
}

group = "net.ltgt.gradle.incap"

java {
    withJavadocJar()
    withSourcesJar()
}

val sonatypeRepository =
    publishing.repositories.maven {
        name = "sonatype"
        url =
            if (isSnapshot) {
                uri("https://oss.sonatype.org/content/repositories/snapshots/")
            } else {
                uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            }
        credentials {
            username = project.findProperty("ossrhUsername") as? String
            password = project.findProperty("ossrhPassword") as? String
        }
    }

fun createPublication(publicationName: String) =
    publishing.publications.create<MavenPublication>(publicationName) {
        from(components["java"])
        afterEvaluate {
            artifactId = base.archivesName.get()
        }

        versionMapping {
            usage("java-api") {
                fromResolutionOf("runtimeClasspath")
            }
            usage("java-runtime") {
                fromResolutionResult()
            }
        }

        pom {
            name = provider { "$groupId:$artifactId" }
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

val mavenPublication = createPublication("maven")

tasks.withType<PublishToMavenRepository>().configureEach {
    if (repository == sonatypeRepository) {
        onlyIf { publication == mavenPublication && publication.version != Project.DEFAULT_VERSION }
    }
}

signing {
    useGpgCmd()
    isRequired = !isSnapshot
    sign(mavenPublication)
}

inline val Project.isSnapshot
    get() = version.toString().endsWith("-SNAPSHOT") || version == Project.DEFAULT_VERSION

//
// For integration tests
//
// Inspired by https://github.com/sigstore/sigstore-java/pull/264/files

// name must already be capitalized for computing task name below
val localPublication = createPublication("Local")

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
            onlyIf { publication == localPublication }
            dependsOn(cleanLocalRepository)
        }
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
