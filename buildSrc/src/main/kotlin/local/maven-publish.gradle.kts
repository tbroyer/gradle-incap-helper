package local

plugins {
    id("local.java-library")
    `maven-publish`
    signing
}

group = "net.ltgt.gradle.incap"
if (project != rootProject) {
    version = rootProject.version
}

java {
    withJavadocJar()
    withSourcesJar()
}

val sonatypeRepository = publishing.repositories.maven {
    name = "sonatype"
    url = if (isSnapshot) {
        uri("https://oss.sonatype.org/content/repositories/snapshots/")
    } else {
        uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
    }
    credentials {
        username = project.findProperty("ossrhUsername") as? String
        password = project.findProperty("ossrhPassword") as? String
    }
}

val mavenPublication = publishing.publications.create<MavenPublication>("maven") {
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
        name.set(provider { "$groupId:$artifactId" })
        description.set(provider { project.description ?: name.get() })
        url.set("https://github.com/tbroyer/gradle-incap-helper")
        developers {
            developer {
                name.set("Thomas Broyer")
                email.set("t.broyer@ltgt.net")
            }
        }
        scm {
            connection.set("https://github.com/tbroyer/gradle-incap-helper.git")
            developerConnection.set("scm:git:ssh://github.com:tbroyer/gradle-incap-helper.git")
            url.set("https://github.com/tbroyer/gradle-incap-helper")
        }
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
    }
}

tasks.withType<PublishToMavenRepository>().configureEach {
    onlyIf { if (repository == sonatypeRepository) publication == mavenPublication else true }
}

signing {
    useGpgCmd()
    isRequired = !isSnapshot
    sign(mavenPublication)
}

inline val Project.isSnapshot
    get() = version.toString().endsWith("-SNAPSHOT")
