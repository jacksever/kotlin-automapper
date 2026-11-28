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

import io.github.jacksever.automapper.sample.mapper.asAnimalTarget
import io.github.jacksever.automapper.sample.mapper.asConvertedAnimalTarget
import io.github.jacksever.automapper.sample.mapper.asNullableAnimalTarget
import io.github.jacksever.automapper.sample.mapper.asNullableObjectTarget
import io.github.jacksever.automapper.sample.mapper.asObjectFallbackTarget
import io.github.jacksever.automapper.sample.mapper.asObjectSource
import io.github.jacksever.automapper.sample.mapper.asObjectTarget
import io.github.jacksever.automapper.sample.model.Child
import io.github.jacksever.automapper.sample.model.Dog
import io.github.jacksever.automapper.sample.model.DogSource
import io.github.jacksever.automapper.sample.model.NullableDogSource
import io.github.jacksever.automapper.sample.model.NullableObjectSource
import io.github.jacksever.automapper.sample.model.NullableObjectTarget
import io.github.jacksever.automapper.sample.model.ObjectSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class ObjectAssignmentMappingTest {

    @Test
    fun `test nullable object preserves non-null instance`() {
        val child = Child(id = 7)
        val target = NullableObjectSource(child = child).asObjectTarget()

        assertSame(expected = child, actual = target.child)
    }

    @Test
    fun `test nullable object without fallback throws for null`() {
        assertFailsWith<NullPointerException> {
            NullableObjectSource(child = null).asObjectTarget()
        }
    }

    @Test
    fun `test object can be assigned to nullable property`() {
        val child = Child(id = 7)
        val target = ObjectSource(child = child).asNullableObjectTarget()

        assertSame(expected = child, actual = target.child)
    }

    @Test
    fun `test reverse mapping preserves non-null object instance`() {
        val child = Child(id = 7)
        val source = NullableObjectTarget(child = child).asObjectSource()

        assertSame(expected = child, actual = source.child)
    }

    @Test
    fun `test object runtime fallback preserves source instance`() {
        val child = Child(id = 7)
        val fallback = Child(id = 99)
        val target = NullableObjectSource(child = child).asObjectFallbackTarget(child = fallback)

        assertSame(expected = child, actual = target.child)
    }

    @Test
    fun `test object runtime fallback replaces null`() {
        val fallback = Child(id = 99)
        val target = NullableObjectSource(child = null).asObjectFallbackTarget(child = fallback)

        assertSame(expected = fallback, actual = target.child)
    }

    @Test
    fun `test subtype can be assigned to interface property`() {
        val dog = Dog(id = 7)
        val target = DogSource(animal = dog).asAnimalTarget()

        assertSame(expected = dog, actual = target.animal)
    }

    @Test
    fun `test nullable subtype preserves non-null instance`() {
        val dog = Dog(id = 7)
        val target = NullableDogSource(animal = dog).asAnimalTarget()

        assertSame(expected = dog, actual = target.animal)
    }

    @Test
    fun `test nullable subtype without fallback throws for null`() {
        assertFailsWith<NullPointerException> {
            NullableDogSource(animal = null).asAnimalTarget()
        }
    }

    @Test
    fun `test subtype can be assigned to nullable interface property`() {
        val dog = Dog(id = 7)
        val target = DogSource(animal = dog).asNullableAnimalTarget()

        assertSame(expected = dog, actual = target.animal)
    }

    @Test
    fun `test nullable subtype assigned to nullable interface preserves nullability`() {
        val dog = Dog(id = 7)
        val nonNullTarget = NullableDogSource(animal = dog).asNullableAnimalTarget()
        val nullTarget = NullableDogSource(animal = null).asNullableAnimalTarget()

        assertSame(expected = dog, actual = nonNullTarget.animal)
        assertNull(actual = nullTarget.animal)
    }

    @Test
    fun `test explicit converter takes priority over compatible assignment`() {
        val target = DogSource(animal = Dog(id = 7)).asConvertedAnimalTarget()

        assertEquals(expected = Dog(id = 8), actual = target.animal)
    }
}
