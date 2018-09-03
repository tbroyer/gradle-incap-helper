package local

plugins {
    `maven-publish`
    signing
}

group = "net.ltgt.gradle.incap"
if (project != rootProject) {
    version = rootProject.version
}

val javadoc by tasks.existing
val javadocJar by tasks.registering(Jar::class) {
    classifier = "javadoc"
    from(javadoc)
}

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets["main"].allSource)
}

val sonatypeRepository = publishing.repositories.maven {
    name = "sonatype"
    url = if (isSnapshot)
        uri("https://oss.sonatype.org/content/repositories/snapshots/") else
        uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
    credentials {
        username = project.findProperty("ossrhUsername") as? String
        password = project.findProperty("ossrhPassword") as? String
    }
}

val mavenPublication = publishing.publications.create<MavenPublication>("maven") {
    from(components["java"])
    afterEvaluate {
        artifactId = requireNotNull(base.archivesBaseName)
    }

    // https://github.com/gradle/gradle-native/issues/723
    artifact(javadocJar.get())
    artifact(sourcesJar.get())

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

inline val Project.base: BasePluginConvention
    get() = the()
inline val Project.sourceSets: SourceSetContainer
    get() = the()
inline val Project.publishing: PublishingExtension
    get() = the()

fun Project.signing(configuration: SigningExtension.() -> Unit) =
    configure(configuration)
