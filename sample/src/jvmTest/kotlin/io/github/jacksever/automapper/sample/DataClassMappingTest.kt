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

import io.github.jacksever.automapper.sample.mapper.asComplexTarget
import io.github.jacksever.automapper.sample.mapper.asInlineDefaultValueTarget
import io.github.jacksever.automapper.sample.mapper.asInstantConverterTarget
import io.github.jacksever.automapper.sample.mapper.asNonNullToNonNullConverterTarget
import io.github.jacksever.automapper.sample.mapper.asNonNullToNullableConverterTarget
import io.github.jacksever.automapper.sample.mapper.asNullabilityTarget
import io.github.jacksever.automapper.sample.mapper.asNullableToNonNullConverterTarget
import io.github.jacksever.automapper.sample.mapper.asNullableToNullableConverterTarget
import io.github.jacksever.automapper.sample.mapper.asPriorityTarget
import io.github.jacksever.automapper.sample.mapper.asRenameTarget
import io.github.jacksever.automapper.sample.mapper.asReversibleSource
import io.github.jacksever.automapper.sample.mapper.asReversibleTarget
import io.github.jacksever.automapper.sample.mapper.asReversibleWithConverterSource
import io.github.jacksever.automapper.sample.mapper.asReversibleWithConverterTarget
import io.github.jacksever.automapper.sample.mapper.asRuntimeOptionalDefaultValueTarget
import io.github.jacksever.automapper.sample.mapper.asRuntimeRequiredDefaultValueTarget
import io.github.jacksever.automapper.sample.mapper.asSimpleTarget
import io.github.jacksever.automapper.sample.mapper.asTypeConversionTarget
import io.github.jacksever.automapper.sample.mapper.asUnsafeTarget
import io.github.jacksever.automapper.sample.model.ComplexSource
import io.github.jacksever.automapper.sample.model.InlineDefaultValueSource
import io.github.jacksever.automapper.sample.model.InstantConverterSource
import io.github.jacksever.automapper.sample.model.NonNullToNonNullConverterSource
import io.github.jacksever.automapper.sample.model.NonNullToNullableConverterSource
import io.github.jacksever.automapper.sample.model.NullabilitySource
import io.github.jacksever.automapper.sample.model.NullableToNonNullConverterSource
import io.github.jacksever.automapper.sample.model.NullableToNullableConverterSource
import io.github.jacksever.automapper.sample.model.PrioritySource
import io.github.jacksever.automapper.sample.model.RenameSource
import io.github.jacksever.automapper.sample.model.ReversibleSource
import io.github.jacksever.automapper.sample.model.ReversibleWithConverterSource
import io.github.jacksever.automapper.sample.model.RuntimeOptionalDefaultValueSource
import io.github.jacksever.automapper.sample.model.RuntimeRequiredDefaultValueSource
import io.github.jacksever.automapper.sample.model.SimpleSource
import io.github.jacksever.automapper.sample.model.TypeConversionSource
import io.github.jacksever.automapper.sample.model.UnsafeSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class DataClassMappingTest {

    @Test
    fun `test simple 1-to-1 mapping`() {
        // Given
        val source = SimpleSource(id = 1, name = "test")

        // When
        val target = source.asSimpleTarget()

        // Then
        assertEquals(expected = source.id, actual = target.id)
        assertEquals(expected = source.name, actual = target.name)
    }

    @Test
    fun `test property renaming`() {
        // Given
        val source = RenameSource(sourceId = 10, sourceName = "Renamed")

        // When
        val target = source.asRenameTarget()

        // Then
        assertEquals(expected = source.sourceId, actual = target.targetId)
        assertEquals(expected = source.sourceName, actual = target.targetName)
    }

    @Test
    fun `test reversible property renaming`() {
        // Given
        val source = ReversibleSource(originalId = 15, name = "Reversible")

        // When: Direct mapping
        val target = source.asReversibleTarget()

        // Then: Direct mapping is correct
        assertEquals(expected = source.name, actual = target.name)
        assertEquals(expected = source.originalId, actual = target.mappedId)

        // When: Reverse mapping
        val reversedSource = target.asReversibleSource()

        // Then: Reverse mapping is correct
        assertEquals(expected = target.name, actual = reversedSource.name)
        assertEquals(expected = target.mappedId, actual = reversedSource.originalId)
    }

    @Test
    fun `test inline default value for nullable to non-nullable`() {
        // Given
        val source = InlineDefaultValueSource(id = 20, description = null)

        // When
        val target = source.asInlineDefaultValueTarget()

        // Then
        assertEquals(expected = source.id, actual = target.id)
        assertEquals(expected = "Default", actual = target.description)
    }

    @Test
    fun `test unsafe mapping for nullable to non-nullable`() {
        // Given a source with a null value
        val invalidSource = UnsafeSource(id = 31, description = null)

        // When & Then: an exception should be thrown
        assertFailsWith<NullPointerException> {
            invalidSource.asUnsafeTarget()
        }
    }

    @Test
    fun `test primitive type conversion`() {
        // Given
        val source = TypeConversionSource(id = "42", value = 3.14f, count = 100L)

        // When
        val target = source.asTypeConversionTarget()

        // Then
        assertEquals(expected = 42, actual = target.id)
        assertEquals(expected = "100", actual = target.count)
        assertEquals(expected = 3.14, actual = target.value, absoluteTolerance = 0.001)
    }

    @Test
    fun `test custom instant converter`() {
        // Given
        val now = Clock.System.now()
        val source = InstantConverterSource(id = 1, createdAt = now)

        // When
        val target = source.asInstantConverterTarget()

        // Then
        assertEquals(expected = source.id, actual = target.id)
        assertEquals(expected = now.toEpochMilliseconds(), actual = target.createdAt)
    }

    @Test
    fun `test local converter overrides global`() {
        // Given
        val now = Clock.System.now()
        val source = PrioritySource(id = 1, createdAt = now)

        // When
        val target = source.asPriorityTarget()

        // Then
        assertEquals(expected = 999L, actual = target.createdAt)
    }

    @Test
    fun `test nullable to nullable mapping`() {
        // Given a source with a null value
        val sourceWithNull = NullabilitySource(id = 50, description = null)

        // When
        val targetWithNull = sourceWithNull.asNullabilityTarget()

        // Then
        assertNull(actual = targetWithNull.description)

        // Given a source with a non-null value
        val sourceWithValue = NullabilitySource(id = 51, description = "Has Value")

        // When
        val targetWithValue = sourceWithValue.asNullabilityTarget()

        // Then
        assertEquals(expected = "Has Value", actual = targetWithValue.description)
    }

    @Test
    fun `test complex mapping with multiple rules`() {
        // Given
        val source = ComplexSource(legacyId = "99", content = null, statusNum = 1)

        // When
        val target = source.asComplexTarget()

        // Then
        assertEquals(expected = 99L, actual = target.id) // Type conversion + rename
        assertEquals(expected = "1", actual = target.statusText) // Type conversion + rename
        assertEquals(expected = "Empty", actual = target.content) // Default value
    }

    @Test
    @OptIn(ExperimentalUuidApi::class)
    fun `test mapping with nullable converters`() {
        // Given
        val uuid = Uuid.random()

        // When: T -> U? (non-nullable source to nullable target)
        val source1 = NonNullToNullableConverterSource(uuid = uuid)
        val target1 = source1.asNonNullToNullableConverterTarget()

        // Then
        assertEquals(expected = uuid.toString(), actual = target1.uuid)

        // When: T? -> U (nullable source to non-nullable target)
        val source2NonNull = NullableToNonNullConverterSource(uuid = uuid)
        val target2NonNull = source2NonNull.asNullableToNonNullConverterTarget()
        val source2Null = NullableToNonNullConverterSource(uuid = null)
        val target2Null = source2Null.asNullableToNonNullConverterTarget()

        // Then
        assertEquals(expected = "default-uuid", actual = target2Null.uuid)
        assertEquals(expected = uuid.toString(), actual = target2NonNull.uuid)

        // When: T? -> U? (nullable source to nullable target)
        val source3NonNull = NullableToNullableConverterSource(uuid = uuid)
        val target3NonNull = source3NonNull.asNullableToNullableConverterTarget()
        val source3Null = NullableToNullableConverterSource(uuid = null)
        val target3Null = source3Null.asNullableToNullableConverterTarget()

        // Then
        assertNull(actual = target3Null.uuid)
        assertEquals(expected = uuid.toString(), actual = target3NonNull.uuid)

        // When: T -> U (non-nullable source to non-nullable target)
        val source4 = NonNullToNonNullConverterSource(uuid = uuid)
        val target4 = source4.asNonNullToNonNullConverterTarget()

        // Then
        assertEquals(expected = uuid.toString(), actual = target4.uuid)
    }

    @Test
    @OptIn(ExperimentalUuidApi::class)
    fun `test reversible mapping with converters`() {
        // Given
        val source = ReversibleWithConverterSource(id = 1, uuid = Uuid.random())

        // When: Direct mapping (Uuid -> String)
        val target = source.asReversibleWithConverterTarget()

        // Then: Direct mapping is correct
        assertEquals(expected = source.id, actual = target.id)
        assertEquals(expected = source.uuid.toString(), actual = target.uuid)

        // When: Reverse mapping (String -> uui)
        val reversedSource = target.asReversibleWithConverterSource()

        // Then: Reverse mapping is correct
        assertEquals(expected = target.id, actual = reversedSource.id)
        assertEquals(expected = source.uuid, actual = reversedSource.uuid)
        assertEquals(expected = target.uuid, actual = reversedSource.uuid.toString())
    }

    @Test
    fun `test runtime required default value`() {
        // Given
        val source = RuntimeRequiredDefaultValueSource(id = 1)

        // When
        val target = source.asRuntimeRequiredDefaultValueTarget(description = "RuntimeValue")

        // Then
        assertEquals(expected = 1, actual = target.id)
        assertEquals(expected = "RuntimeValue", actual = target.description)
    }

    @Test
    fun `test runtime optional default value`() {
        // Given
        val source = RuntimeOptionalDefaultValueSource(id = 1, description = null)

        // When: No value provided, should use compile-time default
        val target1 = source.asRuntimeOptionalDefaultValueTarget()

        // Then
        assertEquals(expected = 1, actual = target1.id)
        assertEquals(expected = "OptionalDefault", actual = target1.description)

        // When: Value is provided at runtime
        val target2 =
            source.asRuntimeOptionalDefaultValueTarget(description = "RuntimeOptionalValue")

        // Then
        assertEquals(expected = 1, actual = target2.id)
        assertEquals(expected = "RuntimeOptionalValue", actual = target2.description)

        // Given: Source has a non-null value
        val sourceWithNonNull = source.copy(description = "SourceValue")

        // When: Should use the value from the source, ignoring defaults
        val target3 = sourceWithNonNull.asRuntimeOptionalDefaultValueTarget()
        val target4 =
            sourceWithNonNull.asRuntimeOptionalDefaultValueTarget(description = "ShouldBeIgnored")

        // Then
        assertEquals(expected = "SourceValue", actual = target3.description)
        assertEquals(expected = "SourceValue", actual = target4.description)
    }
}
