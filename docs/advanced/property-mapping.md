# Property Mapping

When mapping between two classes, it's common for properties to have different names. AutoMapper provides the `@PropertyMapping` annotation to handle these scenarios gracefully.

Use the `propertyMappings` array within `@AutoMapper` to define a list of remapping. Both `from` (the name in the source class) and `to` (the name in the target class) parameters are mandatory.

---

## Renaming Data Class Properties

*Models:*
```kotlin
// Domain
data class User(val id: Long, val fullName: String)

// Data Layer
data class UserEntity(val userId: Long, val name: String)
```

*Mapping Definition:*
```kotlin
import io.github.jacksever.automapper.annotation.AutoMapper
import io.github.jacksever.automapper.annotation.PropertyMapping

@AutoMapper(
    propertyMappings = [
        PropertyMapping(from = "id", to = "userId"),
        PropertyMapping(from = "fullName", to = "name")
    ]
)
fun userMapper(user: User): UserEntity
```

*Generated Code:*
```kotlin
fun User.asUserEntity(): UserEntity = UserEntity(
    userId = id,
    name = fullName
)
```

!!! tip
    If the mapping is `reversible`, the processor will automatically generate the reverse mapping for the properties as well (e.g., `userId` to `id`).

## Mapping Dissimilar Sealed Classes

`@PropertyMapping` is also powerful enough to map between `sealed class` or `enum` hierarchies that don't share the same names for their members.

*Models:*
```kotlin
// Domain
enum class Status { OPEN, IN_PROGRESS, CLOSED }

// UI Layer
enum class UiStatus { Active, Pending, Done }
```

*Mapping Definition:*
```kotlin
@AutoMapper(
    propertyMappings = [
        PropertyMapping(from = "OPEN", to = "Active"),
        PropertyMapping(from = "IN_PROGRESS", to = "Pending"),
        PropertyMapping(from = "CLOSED", to = "Done")
    ]
)
fun statusMapper(status: Status): UiStatus
```
