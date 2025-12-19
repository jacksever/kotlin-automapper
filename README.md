# Kotlin AutoMapper

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE) ![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-blue.svg) ![KSP](https://img.shields.io/badge/KSP-2.2.21--2.0.4-blue.svg)

Effortless, type-safe object-to-object mapping in Kotlin. Tired of writing boilerplate code to convert one object to another? This library does it for you at compile time.

Kotlin AutoMapper uses KSP (Kotlin Symbol Processing) to generate extension functions that automatically map your `data`, `enum`, and `sealed` classes. No reflection, no runtime magic—just pure, fast, and safe generated code.

## Features

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

1.  Ensure you have the `ksp` plugin applied in your module's `build.gradle.kts` file.
2.  Add the dependencies to your `build.gradle.kts`:

```kotlin
dependencies {
    // Annotation dependency
    implementation(project(":automapper:annotation"))

    // KSP processor
    ksp(project(":automapper:processor"))
}
```

## How to Use

Using the library is a simple three-step process:

### Step 1: Define Your Models

Let's say you have a domain model and a database entity.

**Domain Model:**
`src/main/kotlin/com/example/domain/User.kt`
```kotlin
data class User(
    val id: Long,
    val name: String,
    val age: Int
)
```

**Entity Model:**
`src/main/kotlin/com/example/entity/UserEntity.kt`
```kotlin
data class UserEntity(
    val id: Long,
    val name: String,
    val age: Int
)
```

### Step 2: Declare Your Mapping Intent

Create an interface and annotate it with `@AutoMapperModule`. Inside, define functions that describe *what* you want to map. The function names do not matter.

`src/main/kotlin/com/example/mapper/MapperModule.kt`
```kotlin
import io.github.jacksever.automapper.annotation.AutoMapper
import io.github.jacksever.automapper.annotation.AutoMapperModule

// Making this interface internal will make the generated code internal
@AutoMapperModule
internal interface MapperModule {
    
    // This will create User.asUserEntity() and UserEntity.asUser()
    @AutoMapper
    fun userMapper(user: User): UserEntity
}
```

### Step 3: Build Your Project

Build your project (`./gradlew build`). KSP will automatically find your `MapperModule` and generate the necessary extension functions.

### What Happens Under the Hood?

For the `MapperModule` defined above, the KSP processor will generate a new file named `UserMapper.kt` inside the `com.example.mapper` package. This file will contain the actual extension functions:

`src/main/kotlin/com/example/mapper/UserMapper.kt` (Generated)
```kotlin
package com.example.mapper

import com.example.domain.User
import com.example.entity.UserEntity

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

### Step 4: Use the Generated Functions

You can now call the generated functions directly on your objects.

```kotlin
import com.example.mapper.asUser
import com.example.mapper.asUserEntity

fun main() {
    val domainUser = User(id = 1, name = "Jane Doe", age = 28)

    // Convert to entity
    val entity = domainUser.asUserEntity()
    println("Entity: $entity")

    // Convert back to domain
    val revertedUser = entity.asUser()
    println("Reverted: $revertedUser")
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

### Enum and Sealed Class Mapping

Mapping `enum` and `sealed` classes works exactly the same way. Just define them in your `MapperModule`, and the processor will generate the necessary code (`valueOf(name)` for enums and a `when` expression for sealed classes).

## Compatibility

-   **Kotlin:** `2.2.21`
-   **KSP:** `2.2.21-2.0.4`

## License

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for details.
