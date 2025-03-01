plugins {
    id("local.java-library")
    id("local.maven-publish")
}

description = "Helper library for building incremental annotation processors"

nullaway {
    annotatedPackages.add("net.ltgt.gradle.incap")
}
