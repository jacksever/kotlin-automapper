plugins {
    kotlin("jvm")
    alias(libs.plugins.com.vanniktech.maven.publish)
}

group = "io.github.jacksever.automapper"
version = "0.2.9"

mavenPublishing {
    coordinates(
        groupId = project.group.toString(),
        artifactId = "processor",
        version = project.version.toString()
    )

    pom {
        name.set("Kotlin AutoMapper Processor")
        description.set("The KSP processor for Kotlin AutoMapper, which generates mapping extension functions")
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

dependencies {
    /**
     * Automapper internal dependencies
     */
    // region Automapper internal dependencies
    implementation(project(":automapper:annotation"))
    // endregion

    /**
     * Kotlinpoet dependencies
     */
    // region Kotlinpoet dependencies
    implementation(libs.com.squareup.kotlinpoet)
    implementation(libs.com.squareup.kotlinpoet.ksp)
    // endregion

    /**
     * Google KSP dependencies
     */
    // region Google KSP dependencies
    implementation(libs.google.devtools.ksp.processor.api)
    // endregion
}
