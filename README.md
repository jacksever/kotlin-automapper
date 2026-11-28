# Kotlin AutoMapper

[![Maven Central](https://img.shields.io/maven-central/v/io.github.jacksever.automapper/annotation)](https://central.sonatype.com/artifact/io.github.jacksever.automapper/annotation)
[![Build and Test](https://github.com/jacksever/kotlin-automapper/actions/workflows/build-and-test.yml/badge.svg?branch=main)](https://github.com/jacksever/kotlin-automapper/actions/workflows/build-and-test.yml)
[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF?logo=kotlin)](https://kotlinlang.org/multiplatform/)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

**Compile-time object and DTO mapping for Kotlin Multiplatform.**

Declare a mapping once and generate Kotlin extension functions in both directions. Kotlin AutoMapper is a Kotlin object mapper powered by Kotlin Symbol Processing (KSP), built for mapping DTOs, domain models, and entities. Mapping rules stay separate from your models, and the generated code is yours to inspect and debug. No reflection or runtime mapping library is required.

[Documentation](https://jacksever.github.io/kotlin-automapper/) · [Installation](#installation) · [Mapping guides](#mapping-capabilities) · [Releases](https://github.com/jacksever/kotlin-automapper/releases)

## See It in Action

Declare your models and a mapper module:

```kotlin
package example

import io.github.jacksever.automapper.annotation.AutoMapper
import io.github.jacksever.automapper.annotation.AutoMapperModule

data class UserDto(val id: Long, val name: String)
data class User(val id: Long, val name: String)

@AutoMapperModule
internal interface UserMapperModule {

    @AutoMapper
    fun map(source: UserDto): User
}
```

KSP generates both directions by default:

```kotlin
internal fun UserDto.asUser(): User = User(
    id = id,
    name = name,
)

internal fun User.asUserDto(): UserDto = UserDto(
    id = id,
    name = name,
)
```

Build the project and call the generated extensions:

```kotlin
fun main() {
    val dto = UserDto(id = 1L, name = "Kotlin")
    val user = dto.asUser()
    val roundTrip = user.asUserDto()

    check(roundTrip == dto)
}
```

Generated extensions use the mapper module's package. Their visibility is `internal` for an internal module and `public` otherwise. The interface declares the mappings; no mapper instance is needed.

## Installation

For a Kotlin/JVM project, use a Gradle version catalog and run Gradle with JDK 21.

Add these entries to `gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "2.4.21"
ksp = "2.3.14"
kotlin-automapper = "0.11.0"

[libraries]
kotlin-automapper-annotation = { module = "io.github.jacksever.automapper:annotation", version.ref = "kotlin-automapper" }
kotlin-automapper-processor = { module = "io.github.jacksever.automapper:processor", version.ref = "kotlin-automapper" }

[plugins]
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

Then configure `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.automapper.annotation)
    ksp(libs.kotlin.automapper.processor)
}

kotlin {
    jvmToolchain(21)
}
```

Place the example above in `src/main/kotlin/Example.kt`, then run `./gradlew build`.

For **Android** or **Kotlin Multiplatform**, follow the [installation guide](https://jacksever.github.io/kotlin-automapper/setup/). KMP projects add the annotation dependency to their source sets and configure the processor for each required KSP target, such as `kspJvm` or `kspIosArm64`. Generated extensions are available according to that compilation's KSP source configuration.

## Mapping Capabilities

| Capability                                  | What it provides                                                                                 | Guide                                                                                                                       |
|---------------------------------------------|--------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| Data classes, enums, and sealed hierarchies | Constructor-based object mapping and generated `when` branches                                   | [Basic usage](https://jacksever.github.io/kotlin-automapper/how-to-use/)                                                    |
| Bidirectional mappings                      | Forward and reverse extensions, with `reversible = false` for one direction                      | [Mapping directions](https://jacksever.github.io/kotlin-automapper/advanced/unidirectional-mapping/)                        |
| Property renaming                           | Match differently named properties or enum/sealed members                                        | [Property mapping](https://jacksever.github.io/kotlin-automapper/advanced/property-mapping/)                                |
| Defaults and nullability                    | Inline defaults or runtime parameters for missing values and nullable-to-required mappings       | [Default values](https://jacksever.github.io/kotlin-automapper/advanced/default-values/)                                    |
| Custom converters                           | Property and collection-element conversions, including generic converter functions               | [Converters](https://jacksever.github.io/kotlin-automapper/advanced/custom-type-conversion/)                                |
| Nested objects and delegated properties     | Construct nested targets from flat source properties and read properties exposed through `by`    | [Nested object mapping](https://jacksever.github.io/kotlin-automapper/advanced/nested-object-mapping/)                      |
| Generic types                               | Concrete and open arguments, upper bounds, inherited members, aliases, and supported projections | [Generic mapping](https://jacksever.github.io/kotlin-automapper/advanced/generic-mapping/)                                  |
| Collections                                 | Convert elements and construct the requested supported mutable or read-only collection           | [Collections](https://jacksever.github.io/kotlin-automapper/advanced/generic-mapping/#inherited-properties-and-collections) |

Custom converters take precedence over built-in conversions. Required `@OptIn` markers from the converters actually used are propagated to generated functions.

## Why Kotlin AutoMapper?

- **Keep mapping rules separate from your models.** Mapper modules hold property mappings, defaults, and converter registration.
- **Use regular Kotlin functions.** Generated extensions work with normal imports, code navigation, and debugging.
- **Catch mapping problems during the build.** The processor reports unsupported definitions and missing required conversions.
- **Keep runtime mapping infrastructure out of your application.** Generated code uses constructor calls, property access, and configured conversions.

Generated code keeps Kotlin's runtime behavior: `String.toInt()` can fail, a nullable-to-required mapping without a configured fallback uses `!!`, and custom converters can throw. Configure defaults and converters to match your data.

## Platforms and Toolchain

The annotation module declares JVM, JavaScript, Wasm JS/WASI, iOS, macOS, watchOS, tvOS, and Linux targets. Kotlin Android projects consume the JVM artifact. The KSP processor runs on the JVM during the build; configure KSP for the target compilations where you need generated mappings.

The repository's [version catalog](gradle/libs.versions.toml) records the Kotlin and KSP versions used by this branch. The [Gradle wrapper](gradle/wrapper/gradle-wrapper.properties) defines its Gradle version. Building the processor requires JDK 21.

See the [release notes](https://github.com/jacksever/kotlin-automapper/releases) for the toolchain changes in published versions.

## Examples and Contributing

Explore the [sample module](sample) for mappings between domain, entity, and UI models. The [behavior tests](sample/src/jvmTest/kotlin/io/github/jacksever/automapper/sample) cover mapping results, including generics, collections, nullability, and custom converters.

```shell
./gradlew :sample:jvmTest :automapper:processor:test
```

[Report a bug or request a feature](https://github.com/jacksever/kotlin-automapper/issues) or read the [contributing guide](CONTRIBUTING.md).

## License

Kotlin AutoMapper is licensed under [Apache License 2.0](LICENSE).
