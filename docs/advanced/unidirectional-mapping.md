# Unidirectional Mapping

If you only need a one-way mapping (e.g., from domain to UI), use the `reversible = false` flag.

```kotlin
@AutoMapperModule
interface MapperModule {
    
    // Only `User.asUiUser()` will be generated
    @AutoMapper(reversible = false)
    fun userUiMapper(user: User): UiUser
}
```
