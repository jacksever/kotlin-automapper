To get started, you need to add the Kotlin AutoMapper dependencies to your project. Ensure the KSP plugin is applied to your module.

## Gradle Setup

We recommend using the [Gradle Version Catalog](https://docs.gradle.org/current/userguide/version_catalogs.html) to manage dependencies.

=== "Version Catalog (Recommended)"

    1.  **Add the dependencies to your `gradle/libs.versions.toml` file:**

        ```toml
        [versions]
        kotlin-automapper = "0.8.0"

        [libraries]
        kotlin-automapper-processor = { group = "io.github.jacksever.automapper", name = "processor", version.ref = "kotlin-automapper" }
        kotlin-automapper-annotation = { group = "io.github.jacksever.automapper", name = "annotation", version.ref = "kotlin-automapper" }
        ```

    2.  **Apply the dependencies in your `build.gradle.kts`:**

        *For a Kotlin Multiplatform project:*

        ```kotlin
        // 1. Add the annotation to your commonMain dependencies
        kotlin {
            sourceSets {
                commonMain.dependencies {
                    implementation(libs.kotlin.automapper.annotation)
                }
            }
        }

        // 2. Apply the processor to the targets you need
        dependencies {
            add("kspJs", libs.kotlin.automapper.processor)
            add("kspJvm", libs.kotlin.automapper.processor)
            add("kspIosArm64", libs.kotlin.automapper.processor)
            // etc. for your other targets
        }
        ```

        *For an Android-only or JVM project:*

        ```kotlin
        dependencies {
            implementation(libs.kotlin.automapper.annotation)
            ksp(libs.kotlin.automapper.processor)
        }
        ```

=== "String Literals"

    If you are not using a version catalog, you can add the dependencies directly.

    *For a Kotlin Multiplatform project:*

    ```kotlin
    // 1. Add the annotation to your commonMain dependencies
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("io.github.jacksever.automapper:annotation:0.8.0")
            }
        }
    }

    // 2. Apply the processor to the targets you need
    dependencies {
        add("kspJs", "io.github.jacksever.automapper:processor:0.8.0")
        add("kspJvm", "io.github.jacksever.automapper:processor:0.8.0")
        add("kspIosArm64", "io.github.jacksever.automapper:processor:0.8.0")
        // etc. for your other targets
    }
    ```

    *For an Android-only or JVM project:*

    ```kotlin
    dependencies {
        implementation("io.github.jacksever.automapper:annotation:0.8.0")
        ksp("io.github.jacksever.automapper:processor:0.8.0")
    }
    ```
