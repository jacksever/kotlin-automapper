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

import io.github.jacksever.automapper.sample.mapper.asConvertedMutableListTarget
import io.github.jacksever.automapper.sample.mapper.asConvertedMutableSetTarget
import io.github.jacksever.automapper.sample.mapper.asMutableAnimalListTarget
import io.github.jacksever.automapper.sample.mapper.asMutableAnimalSetTarget
import io.github.jacksever.automapper.sample.mapper.asMutableItemListTarget
import io.github.jacksever.automapper.sample.mapper.asMutableItemSetTarget
import io.github.jacksever.automapper.sample.mapper.asMutableListFallbackTarget
import io.github.jacksever.automapper.sample.mapper.asMutableListTarget
import io.github.jacksever.automapper.sample.mapper.asMutableNullableElementListTarget
import io.github.jacksever.automapper.sample.mapper.asMutableSetTarget
import io.github.jacksever.automapper.sample.mapper.asNestedMutableListTarget
import io.github.jacksever.automapper.sample.mapper.asNullableMutableListTarget
import io.github.jacksever.automapper.sample.mapper.asReadOnlyListSource
import io.github.jacksever.automapper.sample.mapper.asReadOnlySetSource
import io.github.jacksever.automapper.sample.model.Animal
import io.github.jacksever.automapper.sample.model.ConverterItem
import io.github.jacksever.automapper.sample.model.MutableItemCollectionSource
import io.github.jacksever.automapper.sample.model.Dog
import io.github.jacksever.automapper.sample.model.MutableDogListSource
import io.github.jacksever.automapper.sample.model.MutableDogSetSource
import io.github.jacksever.automapper.sample.model.MutableListSource
import io.github.jacksever.automapper.sample.model.MutableListTarget
import io.github.jacksever.automapper.sample.model.MutableSetSource
import io.github.jacksever.automapper.sample.model.MutableSetTarget
import io.github.jacksever.automapper.sample.model.NestedMutableListSource
import io.github.jacksever.automapper.sample.model.NullableReadOnlyListSource
import io.github.jacksever.automapper.sample.model.ReadOnlyListSource
import io.github.jacksever.automapper.sample.model.ReadOnlySetSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class MutableCollectionMappingTest {

    @Test
    fun `test readonly lists produce mutable copies for all sizes`() {
        for (items in listOf(emptyList(), listOf(1), listOf(1, 2))) {
            val target = ReadOnlyListSource(items = items).asMutableListTarget()
            target.items.add(99)

            assertEquals(expected = items + 99, actual = target.items)
            assertEquals(expected = items.size + 1, actual = target.items.size)
        }
    }

    @Test
    fun `test readonly sets produce mutable copies for all sizes`() {
        for (items in listOf(emptySet(), setOf(1), setOf(1, 2))) {
            val target = ReadOnlySetSource(items = items).asMutableSetTarget()
            target.items.add(99)

            assertEquals(expected = items + 99, actual = target.items)
            assertEquals(expected = items.size + 1, actual = target.items.size)
        }
    }

    @Test
    fun `test list to mutable set removes duplicates`() {
        val source = ReadOnlyListSource(items = listOf(1, 1, 2))
        val target = source.asMutableSetTarget()
        target.items.add(3)

        assertEquals(expected = setOf(1, 2, 3), actual = target.items)
        assertEquals(expected = listOf(1, 1, 2), actual = source.items)
    }

    @Test
    fun `test set to mutable list preserves iteration order`() {
        val target = ReadOnlySetSource(items = linkedSetOf(2, 1)).asMutableListTarget()
        target.items.add(3)

        assertEquals(expected = listOf(2, 1, 3), actual = target.items)
    }

    @Test
    fun `test converted list remains mutable`() {
        val target = ReadOnlyListSource(items = listOf(1, 2)).asConvertedMutableListTarget()
        target.items.add(3L)

        assertEquals(expected = listOf(1L, 2L, 3L), actual = target.items)
    }

    @Test
    fun `test converted set remains mutable and removes duplicates`() {
        val target = ReadOnlyListSource(items = listOf(1, 1, 2)).asConvertedMutableSetTarget()
        target.items.add(3L)

        assertEquals(expected = setOf(1L, 2L, 3L), actual = target.items)
    }

    @Test
    fun `test mutable list with converted elements remains mutable`() {
        val source = MutableListSource(items = mutableListOf(1, 2))
        val target = source.asConvertedMutableListTarget()
        target.items.add(3L)

        assertEquals(expected = listOf(1L, 2L, 3L), actual = target.items)
        assertEquals(expected = listOf(1, 2), actual = source.items)
    }

    @Test
    fun `test mutable set with converted elements remains mutable`() {
        val target = MutableSetSource(items = mutableSetOf(1, 2)).asConvertedMutableSetTarget()
        target.items.add(3L)

        assertEquals(expected = setOf(1L, 2L, 3L), actual = target.items)
    }

    @Test
    fun `test custom converter produces mutable list`() {
        val source = MutableItemCollectionSource(items = listOf(ConverterItem(1)))
        val target = source.asMutableItemListTarget()
        target.items.add("added")

        assertEquals(expected = listOf("item:1", "added"), actual = target.items)
    }

    @Test
    fun `test custom converter produces mutable set`() {
        val source = MutableItemCollectionSource(items = listOf(ConverterItem(1), ConverterItem(1)))
        val target = source.asMutableItemSetTarget()
        target.items.add("added")

        assertEquals(expected = setOf("item:1", "added"), actual = target.items)
    }

    @Test
    fun `test nested collections remain mutable at both levels`() {
        val target = NestedMutableListSource(items = listOf(listOf(1))).asNestedMutableListTarget()
        target.items[0].add(2L)
        target.items.add(mutableListOf(3L))

        assertEquals(expected = listOf(listOf(1L, 2L), listOf(3L)), actual = target.items)
    }

    @Test
    fun `test nullable collection preserves null`() {
        val target = NullableReadOnlyListSource(items = null).asNullableMutableListTarget()

        assertNull(actual = target.items)
    }

    @Test
    fun `test nullable collection converts non-null value to mutable list`() {
        val target = NullableReadOnlyListSource(items = listOf(1)).asNullableMutableListTarget()
        val items = requireNotNull(target.items)
        items.add(2)

        assertEquals(expected = listOf(1, 2), actual = items)
    }

    @Test
    fun `test null collection without fallback throws for required mutable target`() {
        assertFailsWith<NullPointerException> {
            NullableReadOnlyListSource(items = null).asMutableListTarget()
        }
    }

    @Test
    fun `test null collection uses mutable runtime fallback`() {
        val fallback = mutableListOf(7)
        val target = NullableReadOnlyListSource(items = null).asMutableListFallbackTarget(items = fallback)

        assertSame(expected = fallback, actual = target.items)
    }

    @Test
    fun `test non-null collection ignores fallback and produces mutable copy`() {
        val fallback = mutableListOf(7)
        val target = NullableReadOnlyListSource(items = listOf(1)).asMutableListFallbackTarget(items = fallback)
        target.items.add(2)

        assertEquals(expected = listOf(1, 2), actual = target.items)
        assertEquals(expected = listOf(7), actual = fallback)
    }

    @Test
    fun `test mutable list can widen element type without mutating source`() {
        val dog = Dog(id = 1)
        val source = MutableDogListSource(items = mutableListOf(dog))
        val target = source.asMutableAnimalListTarget()
        val other = object : Animal {}
        target.items.add(other)

        assertEquals(expected = listOf(dog, other), actual = target.items)
        assertEquals(expected = listOf(dog), actual = source.items)
    }

    @Test
    fun `test mutable set can widen element type without mutating source`() {
        val dog = Dog(id = 1)
        val source = MutableDogSetSource(items = mutableSetOf(dog))
        val target = source.asMutableAnimalSetTarget()
        val other = object : Animal {}
        target.items.add(other)

        assertEquals(expected = setOf(dog, other), actual = target.items)
        assertEquals(expected = setOf(dog), actual = source.items)
    }

    @Test
    fun `test mutable list can widen element nullability without mutating source`() {
        val source = MutableListSource(items = mutableListOf(1))
        val target = source.asMutableNullableElementListTarget()
        target.items.add(null)

        assertEquals(expected = listOf(1, null), actual = target.items)
        assertEquals(expected = listOf(1), actual = source.items)
    }

    @Test
    fun `test directly assignable mutable collections preserve instance`() {
        val list = mutableListOf(1)
        val set = mutableSetOf(1)

        assertSame(expected = list, actual = MutableListSource(items = list).asMutableListTarget().items)
        assertSame(expected = set, actual = MutableSetSource(items = set).asMutableSetTarget().items)
    }

    @Test
    fun `test reverse mapping to readonly collection preserves instance`() {
        val list = mutableListOf(1)
        val set = mutableSetOf(1)

        assertSame(expected = list, actual = MutableListTarget(items = list).asReadOnlyListSource().items)
        assertSame(expected = set, actual = MutableSetTarget(items = set).asReadOnlySetSource().items)
    }
}
