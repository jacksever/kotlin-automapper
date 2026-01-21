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

import io.github.jacksever.automapper.sample.mapper.asEnumDefaultValueTarget
import io.github.jacksever.automapper.sample.mapper.asRenameEnumTarget
import io.github.jacksever.automapper.sample.mapper.asSimpleEnumTarget
import io.github.jacksever.automapper.sample.mapper.asUnmappedEnumTarget
import io.github.jacksever.automapper.sample.model.EnumDefaultValueSource
import io.github.jacksever.automapper.sample.model.Priority
import io.github.jacksever.automapper.sample.model.RenameEnumSource
import io.github.jacksever.automapper.sample.model.RenameEnumTarget
import io.github.jacksever.automapper.sample.model.SimpleEnumSource
import io.github.jacksever.automapper.sample.model.SimpleEnumTarget
import io.github.jacksever.automapper.sample.model.UnmappedEnumSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EnumMappingTest {

    @Test
    fun `test simple enum mapping`() {
        // Given
        val sourceA = SimpleEnumSource.A
        val sourceB = SimpleEnumSource.B

        // When
        val targetA = sourceA.asSimpleEnumTarget()
        val targetB = sourceB.asSimpleEnumTarget()

        // Then
        assertEquals(expected = SimpleEnumTarget.A, actual = targetA)
        assertEquals(expected = SimpleEnumTarget.B, actual = targetB)
    }

    @Test
    fun `test enum mapping with renaming`() {
        // Given
        val sourceActive = RenameEnumSource.USER_ACTIVE
        val sourcePending = RenameEnumSource.PENDING_APPROVAL

        // When
        val targetActive = sourceActive.asRenameEnumTarget()
        val targetPending = sourcePending.asRenameEnumTarget()

        // Then
        assertEquals(expected = RenameEnumTarget.ACTIVE, actual = targetActive)
        assertEquals(expected = RenameEnumTarget.PENDING, actual = targetPending)
    }

    @Test
    fun `test unmapped enum constant throws exception`() {
        // Given
        val unmappedSource = UnmappedEnumSource.UNMAPPED

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            unmappedSource.asUnmappedEnumTarget()
        }
    }

    @Test
    fun `test default value for enum property`() {
        // Given
        val source = EnumDefaultValueSource(id = 1)

        // When
        val target = source.asEnumDefaultValueTarget()

        // Then
        assertEquals(expected = source.id, actual = target.id)
        assertEquals(expected = Priority.HIGH, actual = target.priority)
    }
}
