import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.com.vanniktech.maven.publish)
}

group = "io.github.jacksever.automapper"
version = "0.2.0"

kotlin {
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    js(IR) {
        nodejs()
        browser()
        binaries.executable()
    }

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
                name.set("Alexaner Gorodnikov")
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
