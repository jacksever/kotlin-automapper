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

package io.github.jacksever.automapper.sample

import io.github.jacksever.automapper.sample.mapper.asAliasGenericSource
import io.github.jacksever.automapper.sample.mapper.asAliasGenericTarget
import io.github.jacksever.automapper.sample.mapper.asBoundedConverterTarget
import io.github.jacksever.automapper.sample.mapper.asBoundedGenericSource
import io.github.jacksever.automapper.sample.mapper.asBoundedGenericTarget
import io.github.jacksever.automapper.sample.mapper.asContravariantGenericTarget
import io.github.jacksever.automapper.sample.mapper.asGenericCollectionSource
import io.github.jacksever.automapper.sample.mapper.asGenericCollectionTarget
import io.github.jacksever.automapper.sample.mapper.asGenericConvertedCollectionTarget
import io.github.jacksever.automapper.sample.mapper.asGenericConvertedTarget
import io.github.jacksever.automapper.sample.mapper.asGenericInheritedTarget
import io.github.jacksever.automapper.sample.mapper.asGenericNestedTarget
import io.github.jacksever.automapper.sample.mapper.asGenericRequiredTarget
import io.github.jacksever.automapper.sample.mapper.asGenericRuntimeTarget
import io.github.jacksever.automapper.sample.mapper.asGenericSealedSource
import io.github.jacksever.automapper.sample.mapper.asGenericSealedTarget
import io.github.jacksever.automapper.sample.mapper.asGenericSource
import io.github.jacksever.automapper.sample.mapper.asGenericTarget
import io.github.jacksever.automapper.sample.mapper.asIndependentGenericTarget
import io.github.jacksever.automapper.sample.mapper.asIndirectGenericSource
import io.github.jacksever.automapper.sample.mapper.asIndirectGenericTarget
import io.github.jacksever.automapper.sample.mapper.asInheritedConverterTarget
import io.github.jacksever.automapper.sample.mapper.asInlineGenericTarget
import io.github.jacksever.automapper.sample.mapper.asModuleGenericTarget
import io.github.jacksever.automapper.sample.mapper.asNestedObjectGenericTarget
import io.github.jacksever.automapper.sample.mapper.asNullableBoundedGenericTarget
import io.github.jacksever.automapper.sample.mapper.asNullableGenericConverterTarget
import io.github.jacksever.automapper.sample.mapper.asOverloadedDefaultTarget
import io.github.jacksever.automapper.sample.mapper.asProjectedGenericTarget
import io.github.jacksever.automapper.sample.mapper.asRankedGenericTarget
import io.github.jacksever.automapper.sample.mapper.asReifiedGenericTarget
import io.github.jacksever.automapper.sample.mapper.asRejectedBoundConverterTarget
import io.github.jacksever.automapper.sample.mapper.asReorderedGenericSource
import io.github.jacksever.automapper.sample.mapper.asReorderedGenericTarget
import io.github.jacksever.automapper.sample.mapper.asRepeatedGenericTarget
import io.github.jacksever.automapper.sample.mapper.asReturnOnlyGenericTarget
import io.github.jacksever.automapper.sample.mapper.asSpecializedGenericTarget
import io.github.jacksever.automapper.sample.mapper.asStarGenericTarget
import io.github.jacksever.automapper.sample.mapper.asSupertypeConverterTarget
import io.github.jacksever.automapper.sample.mapper.asUpperBoundConverterTarget
import io.github.jacksever.automapper.sample.mapper.asUpperBoundGenericTarget
import io.github.jacksever.automapper.sample.model.BoundedConverterSource
import io.github.jacksever.automapper.sample.model.BoundedGenericSource
import io.github.jacksever.automapper.sample.model.BoundedGenericTarget
import io.github.jacksever.automapper.sample.model.ContravariantGenericSource
import io.github.jacksever.automapper.sample.model.GenericCollectionSource
import io.github.jacksever.automapper.sample.model.GenericConvertedCollectionSource
import io.github.jacksever.automapper.sample.model.GenericConvertedSource
import io.github.jacksever.automapper.sample.model.GenericConvertedTarget
import io.github.jacksever.automapper.sample.model.GenericConverterValue
import io.github.jacksever.automapper.sample.model.GenericInheritedSource
import io.github.jacksever.automapper.sample.model.GenericInheritedTarget
import io.github.jacksever.automapper.sample.model.GenericNestedSource
import io.github.jacksever.automapper.sample.model.GenericNestedTarget
import io.github.jacksever.automapper.sample.model.GenericNullableSource
import io.github.jacksever.automapper.sample.model.GenericRequiredTarget
import io.github.jacksever.automapper.sample.model.GenericRuntimeSource
import io.github.jacksever.automapper.sample.model.GenericSealedSource
import io.github.jacksever.automapper.sample.model.GenericSealedTarget
import io.github.jacksever.automapper.sample.model.GenericSource
import io.github.jacksever.automapper.sample.model.GenericTarget
import io.github.jacksever.automapper.sample.model.IndependentGenericSource
import io.github.jacksever.automapper.sample.model.IndependentGenericTarget
import io.github.jacksever.automapper.sample.model.IndirectGenericSource
import io.github.jacksever.automapper.sample.model.IndirectGenericTarget
import io.github.jacksever.automapper.sample.model.InheritedConverterSource
import io.github.jacksever.automapper.sample.model.InlineGenericSource
import io.github.jacksever.automapper.sample.model.InlineGenericTarget
import io.github.jacksever.automapper.sample.model.ModuleGenericSource
import io.github.jacksever.automapper.sample.model.ModuleGenericTarget
import io.github.jacksever.automapper.sample.model.NestedObjectGenericSource
import io.github.jacksever.automapper.sample.model.NestedObjectGenericTarget
import io.github.jacksever.automapper.sample.model.NullableBoundedGenericSource
import io.github.jacksever.automapper.sample.model.NullableGenericConverterSource
import io.github.jacksever.automapper.sample.model.OverloadedDefaultSource
import io.github.jacksever.automapper.sample.model.ProjectedGenericSource
import io.github.jacksever.automapper.sample.model.RankedGenericIntSource
import io.github.jacksever.automapper.sample.model.RankedGenericLongSource
import io.github.jacksever.automapper.sample.model.ReifiedContainerSource
import io.github.jacksever.automapper.sample.model.ReifiedGenericSource
import io.github.jacksever.automapper.sample.model.RejectedBoundConverterSource
import io.github.jacksever.automapper.sample.model.ReorderedGenericSource
import io.github.jacksever.automapper.sample.model.ReorderedGenericTarget
import io.github.jacksever.automapper.sample.model.RepeatedGenericSource
import io.github.jacksever.automapper.sample.model.ReturnOnlyGenericSource
import io.github.jacksever.automapper.sample.model.SpecializedGenericSource
import io.github.jacksever.automapper.sample.model.SpecializedGenericTarget
import io.github.jacksever.automapper.sample.model.StarGenericSource
import io.github.jacksever.automapper.sample.model.StringAliasSource
import io.github.jacksever.automapper.sample.model.StringAliasTarget
import io.github.jacksever.automapper.sample.model.SupertypeConverterSource
import io.github.jacksever.automapper.sample.model.UpperBoundConverterSource
import io.github.jacksever.automapper.sample.model.UpperBoundGenericSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class GenericMappingTest {

    @Test
    fun `test concrete generic string mapping`() {
        // Given
        val source = GenericSource(payload = "text")

        // When
        val target = source.asGenericTarget()

        // Then
        assertEquals(expected = GenericTarget(payload = "text"), actual = target)
    }

    @Test
    fun `test concrete generic primitive conversion and reverse mapping`() {
        // Given
        val source = GenericSource(payload = 7)

        // When
        val target = source.asGenericTarget()
        val reversedSource = target.asGenericSource()

        // Then
        assertEquals(expected = GenericTarget(payload = 7L), actual = target)
        assertEquals(expected = source, actual = reversedSource)
    }

    @Test
    fun `test generic function retains its type parameter`() {
        // Given
        val value = Any()
        val sourceWithValue = GenericSource(payload = value)
        val sourceWithNull = GenericSource<String?>(payload = null)

        // When
        val targetWithValue = sourceWithValue.asGenericTarget()
        val targetWithNull = sourceWithNull.asGenericTarget()
        val reversedSource = targetWithValue.asGenericSource()

        // Then
        assertSame(expected = value, actual = targetWithValue.payload)
        assertSame(expected = value, actual = reversedSource.payload)
        assertNull(actual = targetWithNull.payload)
    }

    @Test
    fun `test generic collection elements are resolved and remain mutable`() {
        // Given
        val source = GenericCollectionSource(payload = listOf(1, 2))

        // When
        val target = source.asGenericCollectionTarget()
        target.payload.add(3L)
        val reversedSource = target.asGenericCollectionSource()

        // Then
        assertEquals(expected = listOf(1L, 2L, 3L), actual = target.payload)
        assertEquals(expected = listOf(1, 2, 3), actual = reversedSource.payload)
    }

    @Test
    fun `test generic nullable property uses typed runtime fallback`() {
        // Given
        val sourceWithNull = GenericNullableSource<String>(payload = null)
        val sourceWithValue = GenericNullableSource(payload = 7)

        // When
        val targetWithNull = sourceWithNull.asGenericRequiredTarget(payload = "fallback")
        val targetWithValue = sourceWithValue.asGenericRequiredTarget(payload = 99)

        // Then
        assertEquals(
            expected = GenericRequiredTarget(payload = "fallback"),
            actual = targetWithNull
        )
        assertEquals(expected = GenericRequiredTarget(payload = 7), actual = targetWithValue)
    }

    @Test
    fun `test missing generic property becomes typed runtime parameter`() {
        // Given
        val value = Any()
        val source = GenericRuntimeSource<Any>(id = 7)

        // When
        val target = source.asGenericRuntimeTarget(payload = value)

        // Then
        assertEquals(expected = 7, actual = target.id)
        assertSame(expected = value, actual = target.payload)
    }

    @Test
    fun `test recursive construction resolves generic nested target`() {
        // Given
        val source = GenericNestedSource(payload = "nested")

        // When
        val target = source.asGenericNestedTarget()

        // Then
        assertEquals(
            expected = GenericNestedTarget(nested = GenericTarget(payload = "nested")),
            actual = target
        )
    }

    @Test
    fun `test inherited generic property is resolved`() {
        // Given
        val source = GenericInheritedSource(id = 7)

        // When
        val target = source.asGenericInheritedTarget()

        // Then
        assertEquals(
            expected = GenericInheritedTarget(id = 7, inherited = "inherited"),
            actual = target
        )
    }

    @Test
    fun `test function bounds are preserved`() {
        // Given
        val source = BoundedGenericSource(payload = 7)

        // When
        val target = source.asBoundedGenericTarget()
        val reversedSource = target.asBoundedGenericSource()

        // Then
        assertEquals(expected = BoundedGenericTarget(payload = 7), actual = target)
        assertEquals(expected = source, actual = reversedSource)
    }

    @Test
    fun `test module type parameters are declared on generated function`() {
        // Given
        val source = ModuleGenericSource(payload = "module")

        // When
        val target = source.asModuleGenericTarget()

        // Then
        assertEquals(expected = ModuleGenericTarget(payload = "module"), actual = target)
    }

    @Test
    fun `test generic type aliases retain type arguments`() {
        // Given
        val source = StringAliasSource(payload = "alias")

        // When
        val target = source.asAliasGenericTarget()
        val reversedSource = target.asAliasGenericSource()

        // Then
        assertEquals(expected = StringAliasTarget(payload = "alias"), actual = target)
        assertEquals(expected = source, actual = reversedSource)
    }

    @Test
    fun `test generic converter infers its type argument`() {
        // Given
        val source = GenericConvertedSource(payload = GenericConverterValue(payload = 7))

        // When
        val target = source.asGenericConvertedTarget()

        // Then
        assertEquals(
            expected = GenericConvertedTarget(payload = GenericTarget(payload = 7)),
            actual = target
        )
    }

    @Test
    fun `test generic converter resolves collection elements`() {
        // Given
        val source = GenericConvertedCollectionSource(
            payload = listOf(GenericConverterValue(payload = "converted"))
        )

        // When
        val target = source.asGenericConvertedCollectionTarget()

        // Then
        assertEquals(
            expected = listOf(GenericTarget(payload = "converted")),
            actual = target.payload
        )
    }

    @Test
    fun `test generic sealed leaf preserves payload and reverse mapping`() {
        // Given
        val source: GenericSealedSource<String> = GenericSealedSource.Value(payload = "sealed")

        // When
        val target = source.asGenericSealedTarget()
        val reversedSource = target.asGenericSealedSource()

        // Then
        assertEquals(expected = GenericSealedTarget.Value(payload = "sealed"), actual = target)
        assertEquals(expected = source, actual = reversedSource)
    }

    @Test
    fun `test generic sealed object remains exhaustive`() {
        // Given
        val source: GenericSealedSource<String> = GenericSealedSource.Empty

        // When
        val target = source.asGenericSealedTarget()

        // Then
        assertSame(expected = GenericSealedTarget.Empty, actual = target)
    }

    @Test
    fun `test sealed leaf resolves reordered type parameters`() {
        // Given
        val source: ReorderedGenericSource<Int, String> =
            ReorderedGenericSource.Value(first = "first", second = 7)

        // When
        val target = source.asReorderedGenericTarget()
        val reversedSource = target.asReorderedGenericSource()

        // Then
        assertEquals(
            expected = ReorderedGenericTarget.Value(first = "first", second = 7),
            actual = target
        )
        assertEquals(expected = source, actual = reversedSource)
    }

    @Test
    fun `test star projections preserve values`() {
        // Given
        val source: StarGenericSource<*> = StarGenericSource(payload = "star")

        // When
        val target = source.asStarGenericTarget()

        // Then
        assertEquals(expected = "star", actual = target.payload)
    }

    @Test
    fun `test use-site projections preserve values`() {
        // Given
        val source: ProjectedGenericSource<out Number> = ProjectedGenericSource(payload = 7)

        // When
        val target = source.asProjectedGenericTarget()

        // Then
        assertEquals(expected = 7, actual = target.payload)
    }

    @Test
    fun `test unused module parameter does not prevent type inference`() {
        // Given
        val source = IndependentGenericSource(payload = "independent")

        // When
        val target = source.asIndependentGenericTarget()

        // Then
        assertEquals(expected = IndependentGenericTarget(payload = "independent"), actual = target)
    }

    @Test
    fun `test generic converter infers from collection supertype`() {
        // Given
        val source = SupertypeConverterSource(payload = listOf("supertype"))

        // When
        val target = source.asSupertypeConverterTarget()

        // Then
        assertEquals(expected = "supertype", actual = target.payload)
    }

    @Test
    fun `test generic converter accepts compatible upper bound`() {
        // Given
        val source = BoundedConverterSource(payload = 7)

        // When
        val target = source.asBoundedConverterTarget()

        // Then
        assertEquals(expected = "number:7", actual = target.payload)
    }

    @Test
    fun `test generic converter ignores incompatible upper bound`() {
        // Given
        val source = RejectedBoundConverterSource(payload = "unchanged")

        // When
        val target = source.asRejectedBoundConverterTarget()

        // Then
        assertEquals(expected = "unchanged", actual = target.payload)
    }

    @Test
    fun `test inherited converter resolves owner type arguments`() {
        // Given
        val source = InheritedConverterSource(payload = 7)

        // When
        val target = source.asInheritedConverterTarget()

        // Then
        assertEquals(expected = "inherited:7", actual = target.payload)
    }

    @Test
    fun `test generic converter preserves nullable input`() {
        // Given
        val sourceWithValue =
            NullableGenericConverterSource(payload = GenericConverterValue(payload = 7))
        val sourceWithNull = NullableGenericConverterSource(payload = null)

        // When
        val targetWithValue = sourceWithValue.asNullableGenericConverterTarget()
        val targetWithNull = sourceWithNull.asNullableGenericConverterTarget()

        // Then
        assertEquals(expected = GenericTarget(payload = 7), actual = targetWithValue.payload)
        assertNull(actual = targetWithNull.payload)
    }

    @Test
    fun `test generic sealed leaf converts concrete arguments`() {
        // Given
        val source: GenericSealedSource<Int> = GenericSealedSource.Value(payload = 7)

        // When
        val target = source.asGenericSealedTarget()

        // Then
        assertEquals(expected = GenericSealedTarget.Value(payload = 7L), actual = target)
    }

    @Test
    fun `test indirect sealed inheritance resolves type arguments`() {
        // Given
        val source: IndirectGenericSource<List<String>> =
            IndirectGenericSource.Middle.Value(payload = "indirect")

        // When
        val target = source.asIndirectGenericTarget()
        val reversedSource = target.asIndirectGenericSource()

        // Then
        assertEquals(
            expected = IndirectGenericTarget.Middle.Value(payload = "indirect"),
            actual = target
        )
        assertEquals(expected = source, actual = reversedSource)
    }

    @Test
    fun `test incompatible specialized sealed leaf is excluded`() {
        // Given
        val source: SpecializedGenericSource<Int> = SpecializedGenericSource.Value(payload = 7)

        // When
        val target = source.asSpecializedGenericTarget()

        // Then
        assertEquals(expected = SpecializedGenericTarget.Value(payload = 7L), actual = target)
    }

    @Test
    fun `test concrete converter takes priority over generic candidates`() {
        // Given
        val source = RankedGenericIntSource(payload = 7)

        // When
        val target = source.asRankedGenericTarget()

        // Then
        assertEquals(expected = "exact:7", actual = target.payload)
    }

    @Test
    fun `test bounded generic converter takes priority over unconstrained converter`() {
        // Given
        val source = RankedGenericLongSource(payload = 7L)

        // When
        val target = source.asRankedGenericTarget()

        // Then
        assertEquals(expected = "number:7", actual = target.payload)
    }

    @Test
    fun `test repeated generic parameter requires compatible arguments`() {
        // Given
        val source = RepeatedGenericSource(payload = 7 to "text")

        // When
        val target = source.asRepeatedGenericTarget()

        // Then
        assertEquals(expected = "mixed:7:text", actual = target.payload)
    }

    @Test
    fun `test converter infers parameter from expected result`() {
        // Given
        val source = ReturnOnlyGenericSource(payload = 7)

        // When
        val target = source.asReturnOnlyGenericTarget()

        // Then
        assertEquals(expected = emptyList(), actual = target.payload)
    }

    @Test
    fun `test bounded generic converter uses safe call for nullable input`() {
        // Given
        val sourceWithValue = NullableBoundedGenericSource(payload = 7)
        val sourceWithNull = NullableBoundedGenericSource(payload = null)

        // When
        val targetWithValue = sourceWithValue.asNullableBoundedGenericTarget()
        val targetWithNull = sourceWithNull.asNullableBoundedGenericTarget()

        // Then
        assertEquals(expected = "number:7", actual = targetWithValue.payload)
        assertNull(actual = targetWithNull.payload)
    }

    @Test
    fun `test inline default uses substituted generic type`() {
        // Given
        val source = InlineGenericSource(payload = "source")

        // When
        val target = source.asInlineGenericTarget()

        // Then
        assertEquals(
            expected = InlineGenericTarget(payload = "source", label = "inline"),
            actual = target
        )
    }

    @Test
    fun `test contravariant target projection can be constructed`() {
        // Given
        val source = ContravariantGenericSource(payload = "contravariant")

        // When
        val target = source.asContravariantGenericTarget()

        // Then
        assertEquals(expected = "contravariant", actual = target.payload)
    }

    @Test
    fun `test bounded generic property uses nested object mapper`() {
        // Given
        val source = UpperBoundGenericSource(payload = GenericSealedSource.Value(payload = 7))

        // When
        val target = source.asUpperBoundGenericTarget()

        // Then
        assertEquals(expected = GenericSealedTarget.Value(payload = 7L), actual = target.payload)
    }

    @Test
    fun `test nested generic mapper retains converted type arguments`() {
        // Given
        val source = NestedObjectGenericSource(payload = GenericSource(payload = 7))

        // When
        val target = source.asNestedObjectGenericTarget()

        // Then
        assertEquals(
            expected = NestedObjectGenericTarget(payload = GenericTarget(payload = 7L)),
            actual = target
        )
    }

    @Test
    fun `test runtime defaults remain valid non-generic overloads`() {
        // Given
        val source = OverloadedDefaultSource(payload = null)

        // When
        val inlineTarget = source.asOverloadedDefaultTarget()
        val runtimeTarget = source.asOverloadedDefaultTarget(payload = "runtime")

        // Then
        assertEquals(expected = "default", actual = inlineTarget.payload)
        assertEquals(expected = "runtime", actual = runtimeTarget.payload)
    }

    @Test
    fun `test generic converter infers arguments through source upper bound`() {
        // Given
        val source = UpperBoundConverterSource(payload = listOf("bounded-source"))

        // When
        val target = source.asUpperBoundConverterTarget()

        // Then
        assertEquals(expected = "bounded-source", actual = target.payload)
    }

    @Test
    fun `test reified mapper parameter is preserved for inline converter`() {
        // Given
        val source = ReifiedGenericSource(payload = "value")

        // When
        val target = source.asReifiedGenericTarget()

        // Then
        assertEquals(expected = "String", actual = target.payload)
    }

    @Test
    fun `test reified converter accepts list with non-reified element parameter`() {
        // Given
        val source = ReifiedContainerSource(payload = listOf("first", "second"))

        // When
        val target = source.asReifiedGenericTarget()

        // Then
        assertEquals(expected = "List", actual = target.payload)
    }

    @Test
    fun `test reified converter accepts array with concrete element type`() {
        // Given
        val source = ReifiedContainerSource(payload = arrayOf("first", "second"))

        // When
        val target = source.asReifiedGenericTarget()

        // Then
        assertEquals(expected = "Array", actual = target.payload)
    }
}
