plugins {
    id("local.java-library")
}

base.archivesBaseName = "incap-processor"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lib"))

    compileOnly("com.google.auto.service:auto-service:1.0-rc4") { isTransitive = false }
    annotationProcessor("com.google.auto.service:auto-service:1.0-rc4")

    testImplementation("junit:junit:4.12")
    testImplementation("com.google.testing.compile:compile-testing:0.15")
}
