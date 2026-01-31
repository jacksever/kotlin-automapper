# Custom Type Conversion with `@AutoConverter`

While AutoMapper handles primitive conversions (e.g., `Int` to `String`), you often need custom logic for complex types, like converting a `kotlin.uuid.Uuid` to a `String` or a `kotlin.time.Instant` to a `Long`. The `@AutoConverter` annotation is designed for this.

## How It Works

The easiest way to understand custom converters is with a non-nullable example.

1.  **Define Converter Functions:** Create an `object` or `class` to hold your converter logic. Inside, create functions that take one parameter and return the converted type. Annotate these functions with `@AutoConverter`.

2.  **Register the Converter Class:** Add your converter class to the `converters` array in either `@AutoMapperModule` (to make it available globally to all mappers in that module) or `@AutoMapper` (for a specific mapping).

*Models:*
```kotlin
import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi

// Domain
@OptIn(ExperimentalUuidApi::class)
data class User(val id: Uuid)

// Data Layer
data class UserEntity(val id: String)
```

*Converter Definition:*
```kotlin
package com.example.converter

import io.github.jacksever.automapper.annotation.AutoConverter
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object UuidConverter {
    
    @AutoConverter
    fun fromUuid(value: Uuid): String = value.toString()

    @AutoConverter
    fun toUuid(value: String): Uuid = Uuid.parse(uuidString = value)
}
```

*Mapping Definition:*
```kotlin
import com.example.converter.UuidConverter
import io.github.jacksever.automapper.annotation.AutoMapper
import io.github.jacksever.automapper.annotation.AutoMapperModule

@AutoMapperModule(
    // Register the class containing the converters globally
    converters = [UuidConverter::class]
)
interface MapperModule {
    
    // The processor will find the correct converters for both directions
    @AutoMapper(reversible = true)
    fun userMapper(user: User): UserEntity
}
```

## Nullability and Converter Selection

AutoMapper's processor is designed to intelligently handle nullability differences between your properties and your converters. It can automatically generate safe calls (`?.let`) or non-null assertions (`!!`) where necessary.

For instance, if your `User` had a nullable `Uuid?` and your `UserEntity` had a `String?`, you would define converters to handle the nullable types:

```kotlin
@OptIn(ExperimentalUuidApi::class)
object NullableUuidConverter {
    
    @AutoConverter
    fun fromUuid(value: Uuid?): String? = value?.toString()

    @AutoConverter
    fun toUuid(value: String?): Uuid? = value?.let { Uuid.parse(uuidString = value) }
}
```

### Converter Selection Logic

When multiple converters can handle a type conversion, the processor follows a two-step process to find the best candidate:

1.  **Prioritize by Nullability Match:** The processor first searches for converters where the nullability of the input parameter **exactly matches** the nullability of the source property. This is the preferred scenario, as it avoids generating extra null-handling code.

2.  **Find the Most Specific Type:** If multiple converters match (or if no exact nullability match is found), the processor selects the most specific one based on the type hierarchy. For example:
    -   A converter from `String` is more specific than one from `CharSequence` or `Any`.
    -   If two converters are equally specific (e.g., one from `CharSequence` and one from `Serializable` for a `String` property), the processor will issue a warning and pick the first one it finds.

### Example

Consider mapping a `Uuid?` property. You have two available converters:

```kotlin
object UuidConverter {

    // 1. Accepts a non-nullable Uuid
    @AutoConverter
    fun fromNonNullUuid(value: Uuid): String = value.toString()
    
    // 2. Accepts a nullable Uuid
    @AutoConverter
    fun fromNullableUuid(value: Uuid?): String? = value?.toString()
}
```

The processor will choose `fromNullableUuid` because its input type (`Uuid?`) perfectly matches the source property's type. This avoids generating an unnecessary `?.let` block that would be required if it chose `fromNonNullUuid`.

!!! tip "Key Points"

    *   **Reversible Mappings:** If you use `reversible = true`, you must provide converters for both directions (e.g., `Uuid -> String` and `String -> Uuid`) for the generated code to compile.
    *   **Converter Priority:** If you register a converter for the same type pair both globally (`@AutoMapperModule`) and locally (`@AutoMapper`), the **local converter will be used**.
    *   **Nullability Awareness:** The processor can bridge nullability differences, but it will always prefer the most direct and type-safe conversion path available.
    *   **`@OptIn` Propagation:** If your converter function, or the class containing it, is marked with an `@OptIn` annotation (like `@OptIn(ExperimentalUuidApi::class)` in the example), the processor will automatically apply that same `@OptIn` annotation to the generated mapping function. This preserves compile-time safety and suppresses warnings in the generated code.
