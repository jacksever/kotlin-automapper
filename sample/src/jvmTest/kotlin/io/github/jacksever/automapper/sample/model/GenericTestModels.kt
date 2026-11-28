/*
 * Copyright (c) 2026 Alexander Gorodnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.jacksever.automapper.sample.model

/** Used for testing generic type resolution */
data class GenericSource<T>(val payload: T)

/** Used for testing generic type resolution */
data class GenericTarget<T>(val payload: T)

/** Used for testing generic type resolution */
data class GenericCollectionSource<T>(val payload: List<T>)

/** Used for testing generic type resolution */
data class GenericCollectionTarget<T>(val payload: MutableList<T>)

/** Used for testing generic type resolution */
data class GenericNullableSource<T>(val payload: T?)

/** Used for testing generic type resolution */
data class GenericRequiredTarget<T>(val payload: T)

/** Used for testing generic type resolution */
data class GenericRuntimeSource<T>(val id: Int)

/** Used for testing generic type resolution */
data class GenericRuntimeTarget<T>(val id: Int, val payload: T)

/** Used for testing generic type resolution */
data class GenericNestedSource<T>(val payload: T)

/** Used for testing generic type resolution */
data class GenericNestedTarget<T>(val nested: GenericTarget<T>)

/** Used for testing generic type resolution */
open class GenericBase<T>(val inherited: T)

/** Used for testing generic type resolution */
data class GenericInheritedSource(val id: Int) : GenericBase<String>("inherited")

/** Used for testing generic type resolution */
data class GenericInheritedTarget(val id: Int, val inherited: String)

/** Used for testing generic type resolution */
data class BoundedGenericSource<T : Number>(val payload: T)

/** Used for testing generic type resolution */
data class BoundedGenericTarget<T : Number>(val payload: T)

/** Used for testing generic type resolution */
data class ModuleGenericSource<T>(val payload: T)

/** Used for testing generic type resolution */
data class ModuleGenericTarget<T>(val payload: T)

/** Used for testing generic type resolution */
data class AliasGenericSource<T>(val payload: T)

/** Used for testing generic type resolution */
data class AliasGenericTarget<T>(val payload: T)

/** Used for testing generic type resolution */
typealias StringAliasSource = AliasGenericSource<String>

/** Used for testing generic type resolution */
typealias StringAliasTarget = AliasGenericTarget<String>

/** Used for testing generic type resolution */
data class GenericConverterValue<T>(val payload: T)

/** Used for testing generic type resolution */
data class GenericConvertedSource(val payload: GenericConverterValue<Int>)

/** Used for testing generic type resolution */
data class GenericConvertedTarget(val payload: GenericTarget<Int>)

/** Used for testing generic type resolution */
data class GenericConvertedCollectionSource(val payload: List<GenericConverterValue<String>>)

/** Used for testing generic type resolution */
data class GenericConvertedCollectionTarget(val payload: List<GenericTarget<String>>)

/** Used for testing generic type resolution */
sealed interface GenericSealedSource<out T> {
    data class Value<T>(val payload: T) : GenericSealedSource<T>
    data object Empty : GenericSealedSource<Nothing>
}

/** Used for testing generic type resolution */
sealed interface GenericSealedTarget<out T> {
    data class Value<T>(val payload: T) : GenericSealedTarget<T>
    data object Empty : GenericSealedTarget<Nothing>
}

/** Used for testing generic type resolution */
sealed interface ReorderedGenericSource<A, B> {
    data class Value<X, Y>(val first: X, val second: Y) : ReorderedGenericSource<Y, X>
}

/** Used for testing generic type resolution */
sealed interface ReorderedGenericTarget<A, B> {
    data class Value<X, Y>(val first: X, val second: Y) : ReorderedGenericTarget<Y, X>
}

/** Used for testing star-projected source and target types */
data class StarGenericSource<T>(val payload: T)

/** Used for testing star-projected source and target types */
data class StarGenericTarget<T>(val payload: T)

/** Used for testing use-site projections */
data class ProjectedGenericSource<T>(val payload: T)

/** Used for testing use-site projections */
data class ProjectedGenericTarget<T>(val payload: T)

/** Used for testing unused generic module parameters */
data class IndependentGenericSource<T>(val payload: T)

/** Used for testing unused generic module parameters */
data class IndependentGenericTarget<T>(val payload: T)

/** Used for testing generic converters on collection supertypes */
data class SupertypeConverterSource(val payload: List<String>)

/** Used for testing generic converters on collection supertypes */
data class SupertypeConverterTarget(val payload: String)

/** Used for testing generic converter bounds */
data class BoundedConverterSource(val payload: Int)

/** Used for testing generic converter bounds */
data class BoundedConverterTarget(val payload: String)

/** Used for testing incompatible generic converter bounds */
data class RejectedBoundConverterSource(val payload: String)

/** Used for testing incompatible generic converter bounds */
data class RejectedBoundConverterTarget(val payload: String)

/** Used for testing inherited converter type arguments */
data class InheritedConverterSource(val payload: Int)

/** Used for testing inherited converter type arguments */
data class InheritedConverterTarget(val payload: String)

/** Used for testing generic converters with nullable input */
data class NullableGenericConverterSource(val payload: GenericConverterValue<Int>?)

/** Used for testing generic converters with nullable input */
data class NullableGenericConverterTarget(val payload: GenericTarget<Int>?)

/** Used for testing substitution through indirect sealed inheritance */
sealed interface IndirectGenericSource<T> {
    sealed interface Middle<X> : IndirectGenericSource<List<X>> {
        data class Value<Y>(val payload: Y) : Middle<Y>
    }
}

/** Used for testing substitution through indirect sealed inheritance */
sealed interface IndirectGenericTarget<T> {
    sealed interface Middle<X> : IndirectGenericTarget<List<X>> {
        data class Value<Y>(val payload: Y) : Middle<Y>
    }
}

/** Used for testing sealed leaves with fixed generic arguments */
sealed interface SpecializedGenericSource<out T> {
    data class Text(val payload: String) : SpecializedGenericSource<String>
    data class Value<T>(val payload: T) : SpecializedGenericSource<T>
}

/** Used for testing sealed leaves with fixed generic arguments */
sealed interface SpecializedGenericTarget<out T> {
    data class Text(val payload: String) : SpecializedGenericTarget<String>
    data class Value<T>(val payload: T) : SpecializedGenericTarget<T>
}

/** Used for testing generic converter inference and constraints */
data class RankedGenericIntSource(val payload: Int)

/** Used for testing generic converter inference and constraints */
data class RankedGenericLongSource(val payload: Long)

/** Used for testing generic converter inference and constraints */
data class RankedGenericTarget(val payload: String)

/** Used for testing generic converter inference and constraints */
data class RepeatedGenericSource(val payload: Pair<Int, String>)

/** Used for testing generic converter inference and constraints */
data class RepeatedGenericTarget(val payload: String)

/** Used for testing generic converter inference and constraints */
data class ReturnOnlyGenericSource(val payload: Int)

/** Used for testing generic converter inference and constraints */
data class ReturnOnlyGenericTarget(val payload: List<String>)

/** Used for testing generic converter inference and constraints */
data class NullableBoundedGenericSource(val payload: Int?)

/** Used for testing generic converter inference and constraints */
data class NullableBoundedGenericTarget(val payload: String?)

/** Used for testing generic converter inference and constraints */
data class InlineGenericSource<T>(val payload: T)

/** Used for testing closed generic inline defaults */
data class InlineGenericTarget<T>(val payload: T, val label: T)

/** Used for testing contravariant target projections */
data class ContravariantGenericSource<T>(val payload: T)

/** Used for testing contravariant target projections */
data class ContravariantGenericTarget<T>(val payload: T)

/** Used for testing object conversion through a type parameter upper bound */
data class UpperBoundGenericSource<T : GenericSealedSource<Int>>(val payload: T)

/** Used for testing object conversion through a type parameter upper bound */
data class UpperBoundGenericTarget(val payload: GenericSealedTarget<Long>)

/** Used for verifying opt-in propagation from instantiated generic converters */
@RequiresOptIn
annotation class ExperimentalGenericMappingApi

/** Used for testing nested generic mapper calls */
data class NestedObjectGenericSource(val payload: GenericSource<Int>)

/** Used for testing nested generic mapper calls */
data class NestedObjectGenericTarget(val payload: GenericTarget<Long>)

/** Used for preserving existing non-generic overloads with runtime defaults */
data class OverloadedDefaultSource(val payload: String?)

/** Used for preserving existing non-generic overloads with runtime defaults */
data class OverloadedDefaultTarget(val payload: String)

/** Used for inferring converter arguments through a mapper parameter upper bound */
data class UpperBoundConverterSource<T : Iterable<String>>(val payload: T)

/** Used for inferring converter arguments through a mapper parameter upper bound */
data class UpperBoundConverterTarget(val payload: String)

/** Used for preserving reified parameters on inline mapper declarations */
data class ReifiedGenericSource<T>(val payload: T)

/** Used for preserving reified parameters on inline mapper declarations */
data class ReifiedGenericTarget(val payload: String)

/** Used for passing parameterized types to reified converters */
data class ReifiedContainerSource<T>(val payload: T)
