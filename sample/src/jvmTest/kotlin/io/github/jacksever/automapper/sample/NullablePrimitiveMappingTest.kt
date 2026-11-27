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

import io.github.jacksever.automapper.sample.mapper.asPrimitiveInlineDefaultTarget
import io.github.jacksever.automapper.sample.mapper.asPrimitiveOptionalDefaultTarget
import io.github.jacksever.automapper.sample.mapper.asPrimitiveRequiredDefaultTarget
import io.github.jacksever.automapper.sample.mapper.asPrimitiveWithoutDefaultTarget
import io.github.jacksever.automapper.sample.model.NullablePrimitiveSource
import io.github.jacksever.automapper.sample.model.PrimitiveInlineDefaultTarget
import io.github.jacksever.automapper.sample.model.PrimitiveOptionalDefaultTarget
import io.github.jacksever.automapper.sample.model.PrimitiveRequiredDefaultTarget
import io.github.jacksever.automapper.sample.model.PrimitiveWithoutDefaultTarget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NullablePrimitiveMappingTest {

    @Test
    fun `test conversion without fallback preserves non-null value`() {
        val source = NullablePrimitiveSource(id = 7)
        val target = source.asPrimitiveWithoutDefaultTarget()

        assertEquals(expected = PrimitiveWithoutDefaultTarget(id = 7L), actual = target)
    }

    @Test
    fun `test conversion without fallback throws for null`() {
        val source = NullablePrimitiveSource(id = null)

        assertFailsWith<NullPointerException> {
            source.asPrimitiveWithoutDefaultTarget()
        }
    }

    @Test
    fun `test inline fallback preserves non-null value`() {
        val source = NullablePrimitiveSource(id = 7)
        val target = source.asPrimitiveInlineDefaultTarget()

        assertEquals(expected = PrimitiveInlineDefaultTarget(id = 7L), actual = target)
    }

    @Test
    fun `test inline fallback replaces null`() {
        val source = NullablePrimitiveSource(id = null)
        val target = source.asPrimitiveInlineDefaultTarget()

        assertEquals(expected = PrimitiveInlineDefaultTarget(id = 0L), actual = target)
    }

    @Test
    fun `test required runtime fallback preserves non-null value`() {
        val source = NullablePrimitiveSource(id = 7)
        val target = source.asPrimitiveRequiredDefaultTarget(id = 123L)

        assertEquals(expected = PrimitiveRequiredDefaultTarget(id = 7L), actual = target)
    }

    @Test
    fun `test required runtime fallback replaces null`() {
        val source = NullablePrimitiveSource(id = null)
        val target = source.asPrimitiveRequiredDefaultTarget(id = 123L)

        assertEquals(expected = PrimitiveRequiredDefaultTarget(id = 123L), actual = target)
    }

    @Test
    fun `test optional runtime fallback preserves non-null value`() {
        val source = NullablePrimitiveSource(id = 7)
        val target = source.asPrimitiveOptionalDefaultTarget()

        assertEquals(expected = PrimitiveOptionalDefaultTarget(id = 7L), actual = target)
    }

    @Test
    fun `test optional runtime fallback replaces null with default`() {
        val source = NullablePrimitiveSource(id = null)
        val target = source.asPrimitiveOptionalDefaultTarget()

        assertEquals(expected = PrimitiveOptionalDefaultTarget(id = 0L), actual = target)
    }

    @Test
    fun `test explicit optional runtime fallback preserves non-null value`() {
        val source = NullablePrimitiveSource(id = 7)
        val target = source.asPrimitiveOptionalDefaultTarget(id = 123L)

        assertEquals(expected = PrimitiveOptionalDefaultTarget(id = 7L), actual = target)
    }

    @Test
    fun `test explicit optional runtime fallback replaces null`() {
        val source = NullablePrimitiveSource(id = null)
        val target = source.asPrimitiveOptionalDefaultTarget(id = 123L)

        assertEquals(expected = PrimitiveOptionalDefaultTarget(id = 123L), actual = target)
    }
}
