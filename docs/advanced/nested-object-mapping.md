# Nested Object Mapping

AutoMapper is equipped with powerful features to handle complex object graphs. It can automatically create nested objects through recursive mapping and seamlessly works with Kotlin's interface delegation (`by`).

---

## Recursive Constructor-Based Mapping

This feature activates when a target class requires a constructor parameter for which there is no property of the same name in the source, but the source *does* have properties that match the parameters of the nested object's constructor.

In simpler terms, if you're mapping `Source` to `Target`, and `Target` has a property `nested` of type `Nested`, the processor will automatically try to generate:

```kotlin
nested = Nested(
    id = source.id,
    value = source.value
)
```

*Models:*
```kotlin
// Source with a flat structure
data class Source(val id: Int, val description: String)

// Nested object required by the target
data class Nested(val id: Int, val description: String)

// Target that requires a nested object
data class Target(val nested: Nested)
```

*Mapping Definition:*
```kotlin
@AutoMapper(reversible = false) // Reversible is false because the structure is asymmetric
fun sourceMapper(from: Source): Target
```

*Generated Code:*
```kotlin
internal fun Source.asTarget(): Target = Target(
    nested = Nested(
        id = id,
        description = description,
    )
)
```

!!! warning "Asymmetric Mappings Require `reversible = false`"

    The example above will fail to generate a **reverse** mapper (`Target` -> `Source`) because `Target` does not have `id` and `description` properties at its top level. For such asymmetric structures, you must specify `reversible = false`.

---

## Mapping with Interface Delegation (`by`)

Kotlin's interface delegation provides a powerful way to compose behavior. From AutoMapper's perspective, properties from a delegated interface are treated as if they were declared directly in the class.

This is **not** recursive mapping but direct property-to-property mapping, and it works automatically for both direct and reverse mappings.

*Models:*

`Product` is an interface with `id` and `name`. `CoreProduct` is a concrete implementation. `OnlineProduct` is a decorated product that adds a `url` and delegates the core properties via `by product`.

```kotlin
// Domain Models
interface Product {
    val id: String
    val name: String
}

data class CoreProduct(
    override val id: String,
    override val name: String,
) : Product

data class OnlineProduct(
    private val product: CoreProduct,
    val url: String,
) : Product by product // Delegates id and name to `product`

// Entity
data class ProductEntity(
    val id: String,
    val name: String,
    val url: String,
)
```

*Mapping Definition:*
```kotlin
// This is reversible by default and works correctly
@AutoMapper
fun productEntityMapper(from: ProductEntity): OnlineProduct
```

*Generated Code (Reverse Mapping):*

Because of `by product`, the Kotlin compiler exposes `id` and `name` properties on the `OnlineProduct` class. The processor sees these properties and generates a standard, flat mapping.

```kotlin
internal fun OnlineProduct.asProductEntity(): ProductEntity = ProductEntity(
    id = id,       // Works because `id` is exposed by delegation
    name = name,   // Works because `name` is exposed by delegation
    url = url      // Works because it's a direct property
)
```

!!! tip "Key Takeaway"

    - **Recursive Mapping** handles nested object creation when source properties are flat.
    - **Interface Delegation (`by`)** flattens the property structure, allowing for simple, direct mapping.
