plugins {
    id("local.java-library")
    id("local.maven-publish")
}

base.archivesName = "incap-processor"
description = "Helper annotation processor for building incremental annotation processors"

nullaway {
    annotatedPackages.add("net.ltgt.gradle.incap.processor")
}

dependencies {
    implementation(projects.lib)

    compileOnly(libs.autoService.annotations)
    annotationProcessor(libs.autoService)
    // We could use compileOnlyApi above, but we don't want the dependency in the POM.
    // This is OK because annotation processors aren't regular Java libraries you compile against.
    testCompileOnly(libs.autoService.annotations)

    testImplementation(libs.junit)
    testImplementation(libs.compileTesting)
}
