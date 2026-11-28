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

import io.github.jacksever.automapper.sample.mapper.asEscapedDefaultTarget
import io.github.jacksever.automapper.sample.mapper.asEscapedEnumTarget
import io.github.jacksever.automapper.sample.mapper.asEscapedPropertyTarget
import io.github.jacksever.automapper.sample.model.EscapedDefaultSource
import io.github.jacksever.automapper.sample.model.EscapedDefaultTarget
import io.github.jacksever.automapper.sample.model.EscapedEnumSource
import io.github.jacksever.automapper.sample.model.EscapedEnumTarget
import io.github.jacksever.automapper.sample.model.EscapedNestedTarget
import io.github.jacksever.automapper.sample.model.EscapedPropertySource
import io.github.jacksever.automapper.sample.model.EscapedPropertyTarget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant
import kotlin.uuid.Uuid

class EscapedIdentifierMappingTest {

    @Test
    fun `test escaped properties preserve source values`() {
        val instant = Instant.fromEpochMilliseconds(456L)
        val uuid = Uuid.parse("01234567-89ab-cdef-0123-456789abcdef")
        val source = EscapedPropertySource(7, "renamed", "source", instant, uuid)

        val target = source.asEscapedPropertyTarget(`class` = "runtime")

        assertEquals(
            expected = EscapedPropertyTarget(7L, "renamed", "source", 456L, uuid.toString(), "runtime"),
            actual = target
        )
    }

    @Test
    fun `test escaped properties use default values for null`() {
        val source = EscapedPropertySource(null, "renamed", null, null, null)

        val target = source.asEscapedPropertyTarget(`class` = "runtime")

        assertEquals(
            expected = EscapedPropertyTarget(99L, "renamed", "fallback", 123L, "missing", "runtime"),
            actual = target
        )
    }

    @Test
    fun `test escaped runtime fallback parameters can be overridden`() {
        val source = EscapedPropertySource(null, "renamed", null, null, null)

        val target = source.asEscapedPropertyTarget(`when` = 8L, `is` = 9L, `class` = "runtime")

        assertEquals(
            expected = EscapedPropertyTarget(8L, "renamed", "fallback", 9L, "missing", "runtime"),
            actual = target
        )
    }

    @Test
    fun `test escaped target properties support defaults and recursive mapping`() {
        val target = EscapedDefaultSource(id = 7).asEscapedDefaultTarget()

        assertEquals(
            expected = EscapedDefaultTarget("inline", EscapedEnumTarget.`when`, null, EscapedNestedTarget(7)),
            actual = target
        )
    }

    @Test
    fun `test escaped enum constant maps by name`() {
        assertEquals(
            expected = EscapedEnumTarget.`when`,
            actual = EscapedEnumSource.`when`.asEscapedEnumTarget()
        )
    }

    @Test
    fun `test escaped enum constant can be renamed`() {
        assertEquals(
            expected = EscapedEnumTarget.`object`,
            actual = EscapedEnumSource.`class`.asEscapedEnumTarget()
        )
    }

    @Test
    fun `test unmapped escaped enum constant throws exception`() {
        assertFailsWith<IllegalArgumentException> {
            EscapedEnumSource.`else`.asEscapedEnumTarget()
        }
    }
}
