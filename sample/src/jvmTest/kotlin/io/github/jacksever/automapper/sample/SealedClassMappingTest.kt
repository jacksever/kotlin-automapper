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

import io.github.jacksever.automapper.sample.mapper.asComplexSealedTarget
import io.github.jacksever.automapper.sample.mapper.asSealedWithDefaultTarget
import io.github.jacksever.automapper.sample.mapper.asSimpleSealedTarget
import io.github.jacksever.automapper.sample.mapper.asTargetWithDefaultState
import io.github.jacksever.automapper.sample.model.ComplexSealedSource
import io.github.jacksever.automapper.sample.model.ComplexSealedTarget
import io.github.jacksever.automapper.sample.model.SealedWithDefaultSource
import io.github.jacksever.automapper.sample.model.SealedWithDefaultTarget
import io.github.jacksever.automapper.sample.model.SimpleSealedSource
import io.github.jacksever.automapper.sample.model.SimpleSealedTarget
import io.github.jacksever.automapper.sample.model.SourceWithMissingState
import io.github.jacksever.automapper.sample.model.TaskState
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(value = JUnit4::class)
class SealedClassMappingTest {

    @Test
    fun `test simple sealed mapping`() {
        // Given
        val sourceA = SimpleSealedSource.A
        val sourceB = SimpleSealedSource.B(value = 10)

        // When
        val targetA = sourceA.asSimpleSealedTarget()
        val targetB = sourceB.asSimpleSealedTarget()

        // Then
        assertEquals(expected = SimpleSealedTarget.A, actual = targetA)
        assertEquals(expected = SimpleSealedTarget.B(value = 10), actual = targetB)
    }

    @Test
    fun `test sealed mapping with leaf and property renaming`() {
        // Given
        val source = ComplexSealedSource.LeafSource(sourceName = "test")

        // When
        val target = source.asComplexSealedTarget()

        // Then
        assertTrue(actual = target is ComplexSealedTarget.LeafTarget)
        assertEquals(expected = "test", actual = target.targetName)
    }

    @Test
    fun `test default value for sealed class property`() {
        // Given
        val source = SourceWithMissingState(id = "task-123")

        // When
        val target = source.asTargetWithDefaultState()

        // Then
        assertEquals(expected = source.id, actual = target.id)
        assertEquals(expected = TaskState.Default, actual = target.state)
    }

    @Test
    fun `test default value for property inside sealed leaf`() {
        // Given a source leaf with a null property
        val source = SealedWithDefaultSource.B(id = 1, description = null)

        // When
        val target = source.asSealedWithDefaultTarget()

        // Then the target should be of the correct type and have the default value
        assertTrue(actual = target is SealedWithDefaultTarget.B)
        assertEquals(expected = 1, actual = target.id)
        assertEquals(expected = "Default Description", actual = target.description)

        // Given a source leaf with a non-null property
        val sourceWithValue = SealedWithDefaultSource.B(id = 2, description = "Not Default")

        // When
        val targetWithValue = sourceWithValue.asSealedWithDefaultTarget()

        // Then the target should retain the original value
        assertTrue(actual = targetWithValue is SealedWithDefaultTarget.B)
        assertEquals(expected = 2, actual = targetWithValue.id)
        assertEquals(expected = "Not Default", actual = targetWithValue.description)
    }
}
