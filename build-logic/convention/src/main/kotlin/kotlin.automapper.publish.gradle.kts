import com.vanniktech.maven.publish.MavenPublishBaseExtension
import io.github.jacksever.convention.AutomapperArtifactExtension

plugins {
    id("com.vanniktech.maven.publish")
}

group = "io.github.jacksever.automapper"
version = "0.10.0"

val automapperArtifactExt =
    project.extensions.create<AutomapperArtifactExtension>(name = "automapperArtifact")

afterEvaluate {
    configure<MavenPublishBaseExtension> {
        coordinates(
            groupId = project.group.toString(),
            artifactId = automapperArtifactExt.artifactId.orNull,
            version = project.version.toString()
        )

        pom {
            name.set(automapperArtifactExt.name.orNull)
            description.set(automapperArtifactExt.description.orNull)
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
}
