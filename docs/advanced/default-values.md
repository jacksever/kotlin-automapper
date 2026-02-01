# Providing Default Values

AutoMapper provides a powerful `@DefaultValue` annotation to handle scenarios where a source property is missing or `null`. This allows you to ensure data integrity and avoid nullable types in your target models.

`@DefaultValue` is configured with a `DefaultValueSource` enum, which determines how the default value is provided to the mapper.

---

## Inline Source

This is the simplest and default mode, corresponding to `DefaultValueSource.INLINE`. The value is hardcoded directly into the generated mapper function.

!!! tip
    Use this for static, unchanging default values, like statuses, roles, or empty strings.

*Models:*
```kotlin
enum class Role { ADMIN, GUEST }

// Domain: nullable address, no role
data class User(val id: Long, val address: String?)

// Data Layer: non-nullable address, has a role
data class UserEntity(val id: Long, val address: String, val role: Role)
```

*Mapping Definition:*
```kotlin
import io.github.jacksever.automapper.annotation.DefaultValue
import io.github.jacksever.automapper.annotation.DefaultValueSource

@AutoMapper(
    defaultValues = [
        // Provides a value for 'role' which is missing in the source
        DefaultValue(
            property = "role", 
            value = "Role.GUEST", 
            source = DefaultValueSource.INLINE
        ),
        // Provides a value for 'address' if it's null
        DefaultValue(
            property = "address", 
            value = "Unknown",
            source = DefaultValueSource.INLINE
        )
    ]
)
fun userMapper(user: User): UserEntity
```

*Generated Code:*
```kotlin
fun User.asUserEntity(): UserEntity = UserEntity(
    id = id,
    address = address ?: "Unknown",
    role = Role.GUEST
)
```

---

## Parameter Source

For dynamic scenarios, you can configure `@DefaultValue` to add parameters to the generated function, allowing you to provide default values at runtime.

!!! example "Use Cases"
    - Providing a tenant ID or user ID that is only known at runtime.
    - Using a dynamic configuration value as a fallback.
    - Injecting a dependency from a DI container in your application layer.

### Required Parameter

This mode, corresponding to `DefaultValueSource.PARAMETER`, adds a **required** parameter to the mapping function. The compiler will force you to provide a value every time you call the mapper.

*Mapping Definition:*
```kotlin
@AutoMapper(
    defaultValues = [
        DefaultValue(
            property = "id",
            source = DefaultValueSource.PARAMETER
        )
    ]
)
fun userMapper(user: User): UserEntity
```

*Generated Function Signature:*
```kotlin
fun User.asUserEntity(id: String): UserEntity
```

### Optional Parameter

This mode, corresponding to `DefaultValueSource.PARAMETER_WITH_DEFAULT`, adds an **optional** parameter with a compile-time default. You can override it at runtime, but you don't have to.

*Mapping Definition:*
```kotlin
@AutoMapper(
    defaultValues = [
        DefaultValue(
            property = "address",
            value = "Not Provided",
            source = DefaultValueSource.PARAMETER_WITH_DEFAULT
        )
    ]
)
fun userMapper(user: User): UserEntity
```

*Generated Function Signature:*
```kotlin
fun User.asUserEntity(address: String = "Not Provided"): UserEntity
```

!!! warning "Property Shadowing"
    When a runtime parameter has the same name as a property in the source object (like `address` in the example above), the processor correctly generates `this.address ?: address`. This ensures the source property's value is used if it's not `null`, and the runtime parameter is used only as a fallback.
