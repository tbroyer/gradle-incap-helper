package local

plugins {
    id("local.java-base")
}

spotless {
    kotlin {
        ktlint("0.40.0")
        licenseHeaderFile(rootProject.file("LICENSE.header"))
    }
}
