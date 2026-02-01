import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.com.vanniktech.maven.publish)
}

group = "io.github.jacksever.automapper"
version = "0.10.0"

kotlin {
    // JVM/Android targets
    jvm()

    // JS targets
    js(IR) {
        nodejs()
        browser()
        binaries.executable()
    }

    // Wasm targets
    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
        browser()
        binaries.executable()
    }

    // iOS targets
    iosArm64()
    iosSimulatorArm64()

    // macOS targets
    macosArm64()

    // watchOS targets
    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()

    // tvOS targets
    tvosArm64()
    tvosSimulatorArm64()

    // Linux targets
    linuxX64()
    linuxArm64()
}

mavenPublishing {
    coordinates(
        groupId = project.group.toString(),
        artifactId = "annotation",
        version = project.version.toString()
    )

    pom {
        name.set("Kotlin AutoMapper Annotation")
        description.set("Annotations for the Kotlin AutoMapper KSP library, used to define mapping modules and functions")
        inceptionYear.set("2025")
        url.set("https://github.com/jacksever/kotlin-automapper")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("jacksever")
                name.set("Alexander Gorodnikov")
                email.set("alexander.gorodnikov@gmail.com")
                organization.set("GitHub")
                organizationUrl.set("https://github.com/jacksever")
            }
        }

        scm {
            connection.set("scm:git:github.com/jacksever/kotlin-automapper.git")
            developerConnection.set("scm:git:ssh://github.com/jacksever/kotlin-automapper.git")
            url.set("https://github.com/jacksever/kotlin-automapper/tree/main")
        }

        issueManagement {
            system.set("GitHub")
            url.set("https://github.com/jacksever/kotlin-automapper/issues")
        }
    }

    signAllPublications()
    publishToMavenCentral()
}
