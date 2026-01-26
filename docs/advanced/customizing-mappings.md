# Customizing Mappings

AutoMapper provides powerful annotations to handle mismatches in property names or to provide default values when a source property is missing or `null`.

## Renaming Properties with `@PropertyMapping`

Use the `propertyMappings` array to map properties that have different names between the source and target. Both `from` and `to` parameters are mandatory.

*Models:*
```kotlin
// Domain
data class User(val id: Long, val fullName: String)

// Data Layer
data class UserEntity(val userId: Long, val name: String)
```

*Mapping Definition:*
```kotlin
import io.github.jacksever.automapper.annotation.PropertyMapping

@AutoMapper(
    propertyMappings = [
        PropertyMapping(from = "id", to = "userId"),
        PropertyMapping(from = "fullName", to = "name")
    ]
)
fun userMapper(user: User): UserEntity
```

This also works for renaming `enum` constants or `sealed` class implementations.

## Providing Default Values with `@DefaultValue`

Use the `defaultValues` array to provide a fallback value for a target property. This is useful in two scenarios:
1.  The source class does not have a corresponding property.
2.  The source property is `nullable`, but the target property is not.

The processor will generate code that uses the provided value if the source property is missing or `null`.

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

@AutoMapper(
    defaultValues = [
        // Provides a value for 'role' (an enum) which is missing in the source
        DefaultValue(property = "role", value = "Role.GUEST"),
        // Provides a value for 'address' if it's null
        DefaultValue(property = "address", value = "Unknown")
    ]
)
fun userMapper(user: User): UserEntity
```
The generated code will safely use the Elvis operator for `address` (`address = address ?: "Unknown"`) and will assign the enum constant for `role` (`role = Role.GUEST`).

The same approach works for `sealed class` objects. It's best to use the simple class name (e.g., `TaskState.Idle`), as the processor will automatically add the necessary imports.
