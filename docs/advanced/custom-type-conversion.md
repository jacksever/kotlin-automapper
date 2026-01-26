# Custom Type Conversion with `@AutoConverter`

While AutoMapper handles primitive conversions (e.g., `Int` to `String`), you often need custom logic for complex types, like converting a `kotlin.uuid.Uuid` to a `String` or a `kotlin.time.Instant` to a `Long`. The `@AutoConverter` annotation is designed for this.

## How It Works

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

**Key Points:**

-   **Reversible Mappings:** If you use `reversible = true`, you must provide converters for both directions (e.g., `Uuid -> String` and `String -> Uuid`) for the generated code to compile.
-   **Converter Priority:** If you register a converter for the same type pair both globally (`@AutoMapperModule`) and locally (`@AutoMapper`), the **local converter will be used**, and the processor will issue a compile-time warning.
-   **`@OptIn` Propagation:** If your converter function, or the class containing it, is marked with an `@OptIn` annotation (like `@OptIn(ExperimentalUuidApi::class)` in the example), the processor will automatically apply that same `@OptIn` annotation to the generated mapping function. This preserves compile-time safety and suppresses warnings in the generated code.
