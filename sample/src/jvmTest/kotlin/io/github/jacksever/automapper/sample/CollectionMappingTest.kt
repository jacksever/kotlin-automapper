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

import io.github.jacksever.automapper.sample.mapper.asCollectionSource
import io.github.jacksever.automapper.sample.mapper.asCollectionTarget
import io.github.jacksever.automapper.sample.mapper.asItemSource
import io.github.jacksever.automapper.sample.mapper.asItemTarget
import io.github.jacksever.automapper.sample.model.CollectionSource
import io.github.jacksever.automapper.sample.model.CollectionTarget
import io.github.jacksever.automapper.sample.model.ItemSource
import io.github.jacksever.automapper.sample.model.ItemTarget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CollectionMappingTest {

    @Test
    fun `test collection mapping with List to Set conversion`() {
        // Given
        val source = CollectionSource(
            id = 1L,
            items = listOf(
                ItemSource(id = 10, name = "A"),
                ItemSource(id = 20, name = "B"),
            )
        )

        // When
        val target = source.asCollectionTarget()
        val expectedItems = source.items.map { item -> item.asItemTarget() }.toSet()

        // Then
        assertIs<Set<*>>(value = target.items)
        assertEquals(expected = source.id, actual = target.id)
        assertEquals(expected = expectedItems, actual = target.items)
    }

    @Test
    fun `test collection mapping with Set to List conversion`() {
        // Given
        val source = CollectionTarget(
            id = 2L,
            items = setOf(
                ItemTarget(id = 30, name = "C"),
                ItemTarget(id = 40, name = "D"),
            )
        )

        // When
        val target = source.asCollectionSource()
        val expectedItems = source.items.map { item -> item.asItemSource() }.toSet()

        // Then
        assertIs<List<*>>(value = target.items)
        assertEquals(expected = source.id, actual = target.id)
        assertEquals(expected = expectedItems, actual = target.items.toSet())
    }
}
