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
import io.github.jacksever.automapper.sample.mapper.asSimpleSealedTarget
import io.github.jacksever.automapper.sample.model.ComplexSealedSource
import io.github.jacksever.automapper.sample.model.ComplexSealedTarget
import io.github.jacksever.automapper.sample.model.SimpleSealedSource
import io.github.jacksever.automapper.sample.model.SimpleSealedTarget
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.Test
import kotlin.test.assertEquals

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
        assertEquals(
            expected = true,
            actual = target is ComplexSealedTarget.LeafTarget
        )
        assertEquals(
            expected = "test",
            actual = (target as ComplexSealedTarget.LeafTarget).targetName
        )
    }
}
