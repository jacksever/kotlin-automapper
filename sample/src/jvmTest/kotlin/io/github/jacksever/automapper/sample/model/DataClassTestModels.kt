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

package io.github.jacksever.automapper.sample.model

import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Used for simple 1-to-1 mapping test */
data class SimpleSource(val id: Int, val name: String)

/** Used for simple 1-to-1 mapping test */
data class SimpleTarget(val id: Int, val name: String)

/** Used for testing property renaming */
data class RenameSource(val sourceId: Int, val sourceName: String)

/** Used for testing property renaming */
data class RenameTarget(val targetId: Int, val targetName: String)

/** Used for testing default value mapping (nullable to non-nullable) */
data class DefaultValueSource(val id: Int, val description: String?)

/** Used for testing default value mapping (nullable to non-nullable) */
data class DefaultValueTarget(val id: Int, val description: String)

/** Used for testing unsafe (!!) mapping (nullable to non-nullable without default value) */
data class UnsafeSource(val id: Int, val description: String?)

/** Used for testing unsafe (!!) mapping (nullable to non-nullable without default value) */
data class UnsafeTarget(val id: Int, val description: String)

/** Used for testing primitive type conversion */
data class TypeConversionSource(val id: String, val value: Float, val count: Long)

/** Used for testing primitive type conversion */
data class TypeConversionTarget(val id: Int, val value: Double, val count: String)

/** Used for testing nullable to nullable mapping */
data class NullabilitySource(val id: Int, val description: String?)

/** Used for testing nullable to nullable mapping */
data class NullabilityTarget(val id: Int, val description: String?)

/** Used for testing a complex case combining multiple features */
data class ComplexSource(val legacyId: String, val content: String?, val statusNum: Int)

/** Used for testing a complex case combining multiple features */
data class ComplexTarget(val id: Long, val content: String, val statusText: String)

/** Used for testing reversible mapping with property renaming */
data class ReversibleSource(val originalId: Int, val name: String)

/** Used for testing reversible mapping with property renaming */
data class ReversibleTarget(val mappedId: Int, val name: String)

/** Used for testing custom type converters with [Instant] */
data class InstantConverterSource(val id: Int, val createdAt: Instant)

/** Used for testing custom type converters with [Instant] */
data class InstantConverterTarget(val id: Int, val createdAt: Long)

/** Used for testing local vs. global converter priority */
data class PrioritySource(val id: Int, val createdAt: Instant)

/** Used for testing local vs. global converter priority */
data class PriorityTarget(val id: Int, val createdAt: Long)

/** Used for testing custom type converters with [Uuid] */
@OptIn(ExperimentalUuidApi::class)
data class ReversibleWithConverterSource(val id: Int, val uuid: Uuid)

/** Used for testing custom type converters with [Uuid] */
data class ReversibleWithConverterTarget(val id: Int, val uuid: String)

/** Used for testing converter mapping from a non-nullable property to a nullable property */
@OptIn(ExperimentalUuidApi::class)
data class NonNullToNullableConverterSource(val uuid: Uuid)

/** Used for testing converter mapping from a non-nullable property to a nullable property */
data class NonNullToNullableConverterTarget(val uuid: String?)

/** Used for testing converter mapping from a nullable property to a non-nullable property */
@OptIn(ExperimentalUuidApi::class)
data class NullableToNonNullConverterSource(val uuid: Uuid?)

/** Used for testing converter mapping from a nullable property to a non-nullable property */
data class NullableToNonNullConverterTarget(val uuid: String)

/** Used for testing converter mapping between nullable properties */
@OptIn(ExperimentalUuidApi::class)
data class NullableToNullableConverterSource(val uuid: Uuid?)

/** Used for testing converter mapping between nullable properties */
data class NullableToNullableConverterTarget(val uuid: String?)

/** Used for testing converter mapping between non-nullable properties */
@OptIn(ExperimentalUuidApi::class)
data class NonNullToNonNullConverterSource(val uuid: Uuid)

/** Used for testing converter mapping between non-nullable properties */
data class NonNullToNonNullConverterTarget(val uuid: String)
