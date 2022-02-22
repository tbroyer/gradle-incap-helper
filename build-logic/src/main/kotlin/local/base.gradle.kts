package local

plugins {
    id("com.diffplug.spotless")
}

dependencyLocking {
    lockAllConfigurations()
    lockMode.set(LockMode.STRICT)
}

spotless {
    kotlinGradle {
        ktlint("0.44.0")
    }
}
