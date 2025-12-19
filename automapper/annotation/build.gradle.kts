plugins {
    kotlin("jvm")
    alias(libs.plugins.com.vanniktech.maven.publish)
}

group = "io.github.jacksever.automapper"
version = "0.1.0"

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
