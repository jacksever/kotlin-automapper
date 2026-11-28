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

import io.github.jacksever.automapper.sample.mapper.asCollectionFallbackTarget
import io.github.jacksever.automapper.sample.mapper.asConverterListSource
import io.github.jacksever.automapper.sample.mapper.asConverterListTarget
import io.github.jacksever.automapper.sample.mapper.asConverterSetTarget
import io.github.jacksever.automapper.sample.mapper.asLocalConverterListTarget
import io.github.jacksever.automapper.sample.mapper.asNestedConverterListTarget
import io.github.jacksever.automapper.sample.mapper.asNullableCollectionConverterTarget
import io.github.jacksever.automapper.sample.mapper.asNullableConverterListTarget
import io.github.jacksever.automapper.sample.mapper.asNumberCollectionTarget
import io.github.jacksever.automapper.sample.mapper.asSameTypeCollectionTarget
import io.github.jacksever.automapper.sample.mapper.asUnsafeConverterListTarget
import io.github.jacksever.automapper.sample.mapper.asWholeConverterListTarget
import io.github.jacksever.automapper.sample.model.ConverterItem
import io.github.jacksever.automapper.sample.model.ConverterListSource
import io.github.jacksever.automapper.sample.model.ConverterListTarget
import io.github.jacksever.automapper.sample.model.ConverterSetSource
import io.github.jacksever.automapper.sample.model.NestedConverterListSource
import io.github.jacksever.automapper.sample.model.NullableCollectionConverterSource
import io.github.jacksever.automapper.sample.model.NullableConverterListSource
import io.github.jacksever.automapper.sample.model.NumberCollectionSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class CollectionConverterMappingTest {

    @Test
    fun `test item converter maps list elements`() {
        val source = ConverterListSource(items = listOf(ConverterItem(1), ConverterItem(2)))

        assertEquals(expected = listOf("item:1", "item:2"), actual = source.asConverterListTarget().items)
    }

    @Test
    fun `test item converter preserves empty collection`() {
        val source = ConverterListSource(items = emptyList())

        assertEquals(expected = emptyList(), actual = source.asConverterListTarget().items)
    }

    @Test
    fun `test reverse mapping uses item converter`() {
        val source = ConverterListTarget(items = listOf("item:1", "item:2"))

        assertEquals(expected = listOf(ConverterItem(1), ConverterItem(2)), actual = source.asConverterListSource().items)
    }

    @Test
    fun `test item converter maps list to set`() {
        val source = ConverterListSource(items = listOf(ConverterItem(1), ConverterItem(1), ConverterItem(2)))

        assertEquals(expected = setOf("item:1", "item:2"), actual = source.asConverterSetTarget().items)
    }

    @Test
    fun `test item converter maps set to list`() {
        val source = ConverterSetSource(items = linkedSetOf(ConverterItem(1), ConverterItem(2)))

        assertEquals(expected = listOf("item:1", "item:2"), actual = source.asConverterListTarget().items)
    }

    @Test
    fun `test item converter maps nested collections`() {
        val source = NestedConverterListSource(items = listOf(listOf(ConverterItem(1)), listOf(ConverterItem(2))))

        assertEquals(expected = listOf(listOf("item:1"), listOf("item:2")), actual = source.asNestedConverterListTarget().items)
    }

    @Test
    fun `test local item converter overrides global converter`() {
        val source = ConverterListSource(items = listOf(ConverterItem(1)))

        assertEquals(expected = listOf("local:1"), actual = source.asLocalConverterListTarget().items)
    }

    @Test
    fun `test whole collection converter takes priority over item converter`() {
        val source = ConverterListSource(items = listOf(ConverterItem(1), ConverterItem(2)))

        assertEquals(expected = listOf("whole:2"), actual = source.asWholeConverterListTarget().items)
    }

    @Test
    fun `test item converter is used when element types are identical`() {
        val source = ConverterListSource(items = listOf(ConverterItem(1)))

        assertEquals(expected = listOf(ConverterItem(101)), actual = source.asSameTypeCollectionTarget().items)
    }

    @Test
    fun `test nullable input converter receives null elements`() {
        val source = NullableConverterListSource(items = listOf(ConverterItem(1), null))

        assertEquals(expected = listOf("nullable:1", "missing"), actual = source.asConverterListTarget().items)
    }

    @Test
    fun `test nullable output converter preserves null results`() {
        val source = ConverterListSource(items = listOf(ConverterItem(1), ConverterItem(-1)))

        assertEquals(expected = listOf("item:1", null), actual = source.asNullableConverterListTarget().items)
    }

    @Test
    fun `test null converter result throws for non-null element target`() {
        val source = ConverterListSource(items = listOf(ConverterItem(-1)))

        assertFailsWith<NullPointerException> { source.asConverterListTarget() }
    }

    @Test
    fun `test null element throws for converter requiring non-null input`() {
        val source = NullableConverterListSource(items = listOf(null))

        assertFailsWith<NullPointerException> { source.asUnsafeConverterListTarget() }
    }

    @Test
    fun `test nullable collection and elements preserve nullability`() {
        val source = NullableCollectionConverterSource(items = listOf(ConverterItem(1), null))

        assertEquals(expected = listOf("item:1", null), actual = source.asNullableCollectionConverterTarget().items)
        assertNull(actual = NullableCollectionConverterSource(items = null).asNullableCollectionConverterTarget().items)
    }

    @Test
    fun `test collection fallback replaces null collection`() {
        val source = NullableCollectionConverterSource(items = null)
        val fallback = listOf("fallback")

        assertSame(expected = fallback, actual = source.asCollectionFallbackTarget(items = fallback).items)
    }

    @Test
    fun `test collection fallback preserves converted non-null collection`() {
        val source = NullableCollectionConverterSource(items = listOf(ConverterItem(1)))

        assertEquals(expected = listOf("item:1"), actual = source.asCollectionFallbackTarget(items = listOf("fallback")).items)
    }

    @Test
    fun `test collection fallback does not replace null elements`() {
        val source = NullableCollectionConverterSource(items = listOf(null))

        assertFailsWith<NullPointerException> { source.asCollectionFallbackTarget(items = listOf("fallback")) }
    }

    @Test
    fun `test item converter takes priority over primitive conversion`() {
        val source = NumberCollectionSource(items = listOf(1, 2))

        assertEquals(expected = listOf(101L, 102L), actual = source.asNumberCollectionTarget().items)
    }
}
