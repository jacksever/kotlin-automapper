# Generic Mapping

AutoMapper preserves type arguments in generated extensions and resolves properties and constructor parameters using those arguments. A mapping can use concrete types such as `Source<Int>`, or keep a type parameter such as `Source<T>` in its generated signature.

The examples below are independent. Annotations come from `io.github.jacksever.automapper.annotation`; generated extensions are placed in the mapper module's package.

## Concrete Type Arguments

Concrete arguments let the processor determine the conversion needed for each property:

```kotlin
data class Packet<T>(val payload: T)
data class PacketDto<T>(val payload: T)

@AutoMapperModule
interface PacketMapperModule {

    @AutoMapper
    fun mapNumber(source: Packet<Int>): PacketDto<Long>
}
```

Here, `payload` is resolved as `Int` in the source and `Long` in the target. The generated extensions convert the value in both directions:

```kotlin
fun Packet<Int>.asPacketDto(): PacketDto<Long> = PacketDto<Long>(
    payload = payload.toLong(),
)

fun PacketDto<Long>.asPacket(): Packet<Int> = Packet<Int>(
    payload = payload.toInt(),
)
```

```kotlin
val dto = Packet(payload = 7).asPacketDto()
val original = dto.asPacket()
// dto.payload == 7L; original.payload == 7
```

`reversible` defaults to `true`. Set it to `false` when only the forward mapping is required.

## Open Type Parameters

When the source and target share an unknown type, declare it on the mapper method:

```kotlin
data class Envelope<T>(val payload: T)
data class EnvelopeDto<T>(val payload: T)

@AutoMapperModule
interface EnvelopeMapperModule {

    @AutoMapper
    fun <T> map(source: Envelope<T>): EnvelopeDto<T>
}
```

The generated function retains `T` and assigns the property directly:

```kotlin
fun <T> Envelope<T>.asEnvelopeDto(): EnvelopeDto<T> = EnvelopeDto<T>(
    payload = payload,
)
```

The same extension works with `Envelope<String>`, `Envelope<Int>`, or `Envelope<String?>`. Nullable type arguments are supported; the source and target model instances in the mapper declaration must themselves be non-nullable.

### Upper Bounds and `where` Constraints

Bounds are preserved on generated functions, including multiple constraints:

```kotlin
data class NumericBox<T : Number>(val payload: T)
data class NumericBoxDto<T : Number>(val payload: T)

@AutoMapperModule
interface NumericBoxMapperModule {

    @AutoMapper
    fun <T> map(source: NumericBox<T>): NumericBoxDto<T>
            where T : Number, T : Comparable<T>
}
```

The forward extension has the same constraints:

```kotlin
fun <T> NumericBox<T>.asNumericBoxDto(): NumericBoxDto<T>
        where T : Number, T : Comparable<T> = NumericBoxDto<T>(
    payload = payload,
)
```

### Module Type Parameters

A type parameter can also belong to the module:

```kotlin
data class ModuleSource<T>(val payload: T)
data class ModuleTarget<T>(val payload: T)

@AutoMapperModule
interface ModuleMapperModule<T : CharSequence> {

    @AutoMapper
    fun map(source: ModuleSource<T>): ModuleTarget<T>
}
```

This produces a standalone extension with `<T : CharSequence>`; calling it does not require a module instance. Only parameters referenced by the mapping signature or its bounds are included. Unused module parameters do not become extra parameters that callers would need to infer.

## Inherited Properties and Collections

### Inherited Generic Properties

Inherited members are resolved in the source's actual type context:

```kotlin
open class RecordBase<T>(val inherited: T)

data class Record(val id: Int) : RecordBase<String>("stored")
data class RecordDto(val id: Int, val inherited: String)

@AutoMapperModule
interface RecordMapperModule {

    @AutoMapper(reversible = false)
    fun map(source: Record): RecordDto
}
```

The mapper reads `inherited` as `String`, even though the base declaration uses `T`. It can therefore pass both `id` and `inherited` to the target constructor.

### Collection Element Types

Type substitution applies inside collections, including mutable targets:

```kotlin
data class Page<T>(val items: List<T>)
data class PageDto<T>(val items: MutableList<T>)

@AutoMapperModule
interface PageMapperModule {

    @AutoMapper
    fun map(source: Page<Int>): PageDto<Long>
}
```

The forward mapping creates the requested mutable collection and converts its elements:

```kotlin
fun Page<Int>.asPageDto(): PageDto<Long> = PageDto<Long>(
    items = items.mapTo(mutableListOf()) { value -> value.toLong() },
)
```

Custom element converters use the same inference and selection rules as property converters. Nested object mappings and recursive constructor arguments also use resolved generic types. A conversion between unrelated `T` and `U` still requires a suitable converter or mapping; declaring type parameters does not define a conversion between them.

## Generic Converters

### Reusing One Converter for Different Arguments

A converter can declare function type parameters:

```kotlin
data class Wrapped<T>(val payload: T)
data class WrappedDto<T>(val payload: T)
data class WrappedSource(val value: Wrapped<String>)
data class WrappedTarget(val value: WrappedDto<String>)

object WrappedConverter {

    @AutoConverter
    fun <T> convert(value: Wrapped<T>): WrappedDto<T> =
        WrappedDto(payload = value.payload)
}

@AutoMapperModule(converters = [WrappedConverter::class])
interface WrappedMapperModule {

    @AutoMapper(reversible = false)
    fun map(source: WrappedSource): WrappedTarget
}
```

AutoMapper infers `T = String` for `value`. The same converter can handle `Wrapped<Int>` or elements of a `List<Wrapped<String>>`. Reverse mapping is disabled here because only a forward converter is registered.

Arguments are inferred from the source and, when needed, the expected result type. Every upper bound is checked before the converter is selected. For example, `<T : Number>` excludes a `String` argument.

When several candidates apply, nullability matching and type specificity still participate in selection. A matching concrete converter takes priority over an unconstrained generic one, and generic upper bounds participate in specificity comparisons. Local registration takes priority over module registration for matching declared type pairs.

### Inherited Converter Methods

Registering a concrete owner also resolves the type parameters of its inherited methods:

```kotlin
data class IdentifierSource(val id: Int)
data class IdentifierTarget(val id: String)

open class IdentifierConverterBase<T> {

    @AutoConverter
    fun convert(value: T): String = "id:$value"
}

object IntIdentifierConverter : IdentifierConverterBase<Int>()

@AutoMapperModule(converters = [IntIdentifierConverter::class])
interface IdentifierMapperModule {

    @AutoMapper(reversible = false)
    fun map(source: IdentifierSource): IdentifierTarget
}
```

For this registered object, the inherited method accepts `Int`. Required `@OptIn` markers are collected from the converters actually used, including inherited methods and converters used inside collections.

See [Custom Type Conversion](custom-type-conversion.md) for converter registration and nullability handling.

## Runtime Defaults

Runtime default parameters retain the resolved target property type:

```kotlin
data class OptionalValue<T>(val payload: T?)
data class RequiredValue<T>(val payload: T)

@AutoMapperModule
interface DefaultMapperModule {

    @AutoMapper(
        reversible = false,
        defaultValues = [
            DefaultValue(property = "payload", source = DefaultValueSource.PARAMETER)
        ],
    )
    fun <T> map(source: OptionalValue<T>): RequiredValue<T>
}
```

The generated runtime parameter has type `T`. Its name can match the source property because the source access is qualified:

```kotlin
fun <T> OptionalValue<T>.asRequiredValue(payload: T): RequiredValue<T> = RequiredValue<T>(
    payload = this.payload ?: payload,
)

val result = OptionalValue<String>(payload = null).asRequiredValue(payload = "fallback")
```

For a concrete `RequiredValue<String>` target, the runtime parameter would instead have type `String`. Inline defaults are also formatted using the substituted property type. Default values belong to the forward target and are not copied into the reverse mapping.

See [Providing Default Values](default-values.md) for the available default sources.

## Generic Sealed Hierarchies

Generic arguments are propagated from the sealed root to each compatible leaf:

```kotlin
sealed interface Event<out T> {
    data class Value<T>(val payload: T) : Event<T>
    data object Empty : Event<Nothing>
}

sealed interface EventDto<out T> {
    data class Value<T>(val payload: T) : EventDto<T>
    data object Empty : EventDto<Nothing>
}

@AutoMapperModule
interface EventMapperModule {

    @AutoMapper
    fun <T> map(source: Event<T>): EventDto<T>
}
```

The forward function keeps the leaf's type argument:

```kotlin
fun <T> Event<T>.asEventDto(): EventDto<T> = when (this) {
    is Event.Value<T> -> EventDto.Value<T>(payload = payload)
    Event.Empty -> EventDto.Empty
}
```

Arguments can pass through intermediate sealed declarations or appear in a different order in a leaf. For example, `Value<X, Y> : Root<Y, X>` is resolved by its relationship to the requested root, rather than by parameter position alone.

When a mapping uses a concrete root such as `Event<Int>`, leaves whose fixed arguments are incompatible with that root are excluded. Source and target leaves still need matching names or explicit property mappings.

!!! warning "Leaf parameters must be inferable"

    A leaf such as `Value<A, B> : Root<A>` introduces a `B` that the root cannot determine. The processor reports an error naming that leaf. Use concrete leaf mappings when the root does not provide enough type information.

## Type Aliases and Projections

### Type Aliases

Aliases retain their arguments while being expanded for type resolution:

```kotlin
data class AliasSource<T>(val payload: T)
data class AliasTarget<T>(val payload: T)

typealias TextSource = AliasSource<String>
typealias TextTarget = AliasTarget<String>

@AutoMapperModule
interface AliasMapperModule {

    @AutoMapper
    fun map(source: TextSource): TextTarget
}
```

Generated extensions use the underlying model names, such as `AliasSource<String>.asAliasTarget()`. The alias does not change the mapper's conversion rules or extension naming.

### Use-Site and Star Projections

Projections are preserved in generated function signatures:

```kotlin
data class ProjectionSource<T>(val payload: T)
data class ProjectionTarget<T>(val payload: T)

@AutoMapperModule
interface ProjectionMapperModule {

    @AutoMapper(reversible = false)
    fun map(source: ProjectionSource<out Number>): ProjectionTarget<out Number>
}
```

The target constructor uses invariant arguments even though the return type is projected:

```kotlin
fun ProjectionSource<out Number>.asProjectionTarget(): ProjectionTarget<out Number> =
    ProjectionTarget<Number>(payload = payload)
```

Star projections are supported when upper bounds provide a valid constructor type. For an unbounded `Target<T>`, a `Target<*>` mapping can construct `Target<Any?>`. More restrictive bounds may require explicit arguments.

!!! warning "Some projected targets cannot be constructed"

    A target such as `Target<*>` with `T : Comparable<T>` does not necessarily provide a concrete type satisfying its recursive bound. The processor reports this and asks for concrete target arguments.

## Reified Converters

Reified converters are allowed when Kotlin can supply the required runtime type:

```kotlin
data class Container<T>(val payload: T)
data class TypeDescription(val payload: String)

object TypeDescriptionConverter {

    @AutoConverter
    inline fun <reified R> describe(value: R): String = R::class.simpleName.orEmpty()
}

@AutoMapperModule(converters = [TypeDescriptionConverter::class])
interface TypeDescriptionMapperModule {

    @AutoMapper(reversible = false)
    fun <T> describeList(source: Container<List<T>>): TypeDescription

    @AutoMapper(reversible = false)
    fun describeArray(source: Container<Array<String>>): TypeDescription
}
```

`describeList` does not need a reified `T`: the converter's runtime type is `List<T>`, whose element argument is erased. Arrays retain their runtime element type, so their elements need an additional check.

| Inferred reified argument | Supported? |
| --- | --- |
| `String` | Yes |
| `List<T>` with an ordinary `T` | Yes |
| `Array<String>` | Yes |
| An ordinary type parameter `T` directly | No |
| `Array<T>` with an ordinary `T` | No |
| A reified mapper type parameter `T` | Yes |

Legal inline mapper declarations retain their reified parameters in the generated extension. Reification of a collection type does not make its element type available at runtime.

## Overloads and Diagnostics

Different generic receivers can share the same extension name:

```kotlin
data class OverloadSource<T>(val payload: T)
data class OverloadTarget<T>(val payload: T)

@AutoMapperModule
interface OverloadMapperModule {

    @AutoMapper(reversible = false)
    fun mapText(source: OverloadSource<String>): OverloadTarget<String>

    @AutoMapper(reversible = false)
    fun mapNumber(source: OverloadSource<Int>): OverloadTarget<Long>
}
```

Kotlin callers use `asOverloadTarget()` for both mappings. To distinguish erased JVM signatures, the generated functions receive `@JvmName("asOverloadTarget_mapText")` and `@JvmName("asOverloadTarget_mapNumber")`, based on the original method names.

Return types alone cannot distinguish Kotlin overloads. Two methods mapping the same `Source<String>` to different target arguments can therefore conflict before JVM erasure is considered.

| Declaration or situation | Required action |
| --- | --- |
| `Source<T>` to `Target<U>` with no conversion between their properties | Provide a converter or matching mapper |
| Nullable root model such as `Source<String>?` | Declare non-nullable source and target models; nullable properties and arguments are supported |
| A sealed leaf parameter absent from the root's type relationship | Map concrete leaf types or make the parameter inferable from the root |
| A projected target whose upper bounds do not form a valid constructor type | Specify concrete target arguments |
| Same generated receiver, function name, runtime parameter types, and bounds | Use distinguishable generated Kotlin signatures; changing only the return type or mapper method name is insufficient |
