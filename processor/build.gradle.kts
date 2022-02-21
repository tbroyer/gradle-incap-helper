plugins {
    id("local.java-library")
    id("local.maven-publish")
}

base.archivesBaseName = "incap-processor"
description = "Helper annotation processor for building incremental annotation processors"

nullaway {
    annotatedPackages.add("net.ltgt.gradle.incap.processor")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lib"))

    compileOnly("com.google.auto.service:auto-service-annotations:1.0.1")
    annotationProcessor("com.google.auto.service:auto-service:1.0.1")
    // We could use compileOnlyApi above, but we don't want the dependency in the POM.
    // This is OK because annotation processors aren't regular Java libraries you compile against.
    testCompileOnly("com.google.auto.service:auto-service-annotations:1.0.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.testing.compile:compile-testing:0.19")
}
