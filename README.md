# Kotlin AutoMapper

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE) [![Maven Central](https://img.shields.io/maven-central/v/io.github.jacksever.automapper/annotation)](https://search.maven.org/artifact/io.github.jacksever.automapper/annotation) ![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-blue) ![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-blue.svg) ![KSP](https://img.shields.io/badge/KSP-2.2.21--2.0.4-blue.svg)

Effortless, type-safe object-to-object mapping in Kotlin. Tired of writing boilerplate code to convert one object to another? This library does it for you at compile time, with full support for Kotlin Multiplatform.

Kotlin AutoMapper uses KSP (Kotlin Symbol Processing) to generate extension functions that automatically map your `data`, `enum`, and `sealed` classes. No reflection, no runtime magic - just pure, fast, and safe generated code for all your targets.

## Features

-   **Kotlin Multiplatform:** Designed from the ground up to work with KMP, generating code for `JVM`, `iOS`, `JS`, and `Wasm` targets.
-   **Compile-Time Safety:** All mappings are verified at build time.
-   **Reflection-Free:** Blazing-fast performance at runtime.
-   **Multi-Module Support:** Seamlessly map classes across different Gradle modules, perfect for clean architecture setups.
-   **Visibility Control:** Control the visibility of generated extensions (`public` or `internal`) by setting the visibility of your `@AutoMapperModule` interface.
-   **Supports `data`, `enum`, and `sealed` classes:** Covers most mapping scenarios.
-   **Bidirectional & Unidirectional Mapping:** Generate mappings in one or both directions with a simple `reversible` flag.
-   **Automatic Primitive Conversion:** Handles conversions like `String` to `Int`, `Int` to `Long`, etc., out of the box.
-   **Collection Mapping:** Automatically handles `List` and `Set` transformations.
-   **Zero Runtime Dependencies:** The `annotation` library is `SOURCE`-only, so it doesn't get bundled into your final artifact.

## Setup

Ensure you have the `ksp` plugin applied in your module's `build.gradle.kts` file.

### For a Kotlin Multiplatform Project

 ```kotlin
 kotlin {
     sourceSets {
         commonMain.dependencies {
             // 1. Add the annotation dependency to commonMain
             implementation("io.github.jacksever.automapper:annotation:0.2.4")
         }
     }
 }

 // 2. Apply the processor to the targets you need
 dependencies {
     add("kspJs", "io.github.jacksever.automapper:processor:0.2.4")
     add("kspJvm", "io.github.jacksever.automapper:processor:0.2.4")
     add("kspIosX64", "io.github.jacksever.automapper:processor:0.2.4")
     // etc. for your other targets
 }
 ```

### For an Android-Only (or JVM) Project

In a standard Android or JVM module, you can add the dependencies directly.

 ```kotlin
 dependencies {
     // Annotation dependency
     implementation("io.github.jacksever.automapper:annotation:0.2.4")

     // KSP processor
     ksp("io.github.jacksever.automapper:processor:0.2.4")
 }
 ```

## How to Use

Using the library is a simple three-step process:

### Step 1: Define Your Models

For a KMP project, define your models in `commonMain` so they are accessible from all targets.

### Step 2: Declare Your Mapping Intent

Create an interface (also in `commonMain`) and annotate it with `@AutoMapperModule`. Inside, define functions that describe *what* you want to map.

`src/commonMain/kotlin/com/example/mapper/MapperModule.kt`
```kotlin
import io.github.jacksever.automapper.annotation.AutoMapper
import io.github.jacksever.automapper.annotation.AutoMapperModule

// Making this interface internal will make the generated code internal
@AutoMapperModule
internal interface MapperModule {
    
    // Data class mapping
    @AutoMapper
    fun userMapper(user: User): UserEntity

    // Sealed class mapping
    @AutoMapper
    fun shapeMapper(shape: Shape): ShapeEntity

    // Enum mapping
    @AutoMapper
    fun statusMapper(status: Status): StatusEntity
}
```

### Step 3: Build Your Project

Build your project (`./gradlew build`). KSP will automatically find your `MapperModule` and generate the necessary extension functions for each target.

### What Happens Under the Hood?

KSP generates extension functions for each mapping you defined. Here is what the generated code looks like:

**Data Class Mapping:**

`UserMapper.kt` (Generated)
```kotlin
package com.example.mapper

// Generated code is internal because the MapperModule is internal

/**
 * Converts [User] to [UserEntity]
 */
internal fun User.asUserEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
    age = age,
)

/**
 * Converts [UserEntity] to [User]
 */
internal fun UserEntity.asUser(): User = User(
    id = id,
    name = name,
    age = age,
)
```

**Enum Mapping:**

`StatusMapper.kt` (Generated)
```kotlin
package com.example.mapper

/**
 * Converts [Status] to [StatusEntity]
 */
internal fun Status.asStatusEntity(): StatusEntity = StatusEntity.valueOf(name)

/**
 * Converts [StatusEntity] to [Status]
 */
internal fun StatusEntity.asStatus(): Status = Status.valueOf(name)
```

**Sealed Class Mapping:**

`ShapeMapper.kt` (Generated)
```kotlin
package com.example.mapper

/**
 * Converts [Shape] to [ShapeEntity]
 */
internal fun Shape.asShapeEntity(): ShapeEntity = when (this) {
    Shape.NoShape -> ShapeEntity.NoShape
    is Shape.Square -> ShapeEntity.Square(side = side)
    is Shape.Circle -> ShapeEntity.Circle(radius = radius)
}

/**
 * Converts [ShapeEntity] to [Shape]
 */
internal fun ShapeEntity.asShape(): Shape = when (this) {
    ShapeEntity.NoShape -> Shape.NoShape
    is ShapeEntity.Square -> Shape.Square(side = side)
    is ShapeEntity.Circle -> Shape.Circle(radius = radius)
}
```

### Step 4: Use the Generated Functions

You can now call the generated functions directly from your common or platform-specific code.

```kotlin
import com.example.mapper.asUserEntity

fun main() {
    val domainUser = User(id = 1, name = "Jane Doe", age = 28)

    // Convert to entity
    val entity = domainUser.asUserEntity()
    println("Entity: $entity")
}
```

## Advanced Usage

### Unidirectional Mapping

If you only need a one-way mapping (e.g., from domain to UI), use the `reversible = false` flag.

```kotlin
@AutoMapperModule
interface MapperModule {
    
    // Only `User.asUiUser()` will be generated
    @AutoMapper(reversible = false)
    fun userUiMapper(user: User): UiUser
}
```

## Compatibility

-   **Kotlin:** `2.2.21`
-   **KSP:** `2.2.21-2.0.4`

## License

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for details.
