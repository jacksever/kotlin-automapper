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
internal fun Status.asStatusEntity(): StatusEntity = when (this) {
    Status.ACTIVE -> StatusEntity.ACTIVE
    Status.PENDING -> StatusEntity.PENDING
    Status.INACTIVE -> StatusEntity.INACTIVE
}

/**
 * Converts [StatusEntity] to [Status]
 */
internal fun StatusEntity.asStatus(): Status = when (this) {
    StatusEntity.ACTIVE -> Status.ACTIVE
    StatusEntity.PENDING -> Status.PENDING
    StatusEntity.INACTIVE -> Status.INACTIVE
}
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
