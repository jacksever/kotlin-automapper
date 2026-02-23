/*
 * Copyright (c) 2026 Alexander Gorodnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import io.github.jacksever.convention.AutomapperExtension
import io.github.jacksever.convention.groupId
import io.github.jacksever.convention.versionName

plugins {
    id("com.vanniktech.maven.publish")
}

group = project.groupId
version = project.versionName

val artifact = project.extensions.getByType<AutomapperExtension>().artifact

afterEvaluate {
    configure<MavenPublishBaseExtension> {
        pom {
            name.set(artifact.name.orNull)
            description.set(artifact.description.orNull)
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
