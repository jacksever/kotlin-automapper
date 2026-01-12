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

/** Used for simple 1-to-1 enum mapping test */
enum class SimpleEnumSource { A, B, C }

/** Used for simple 1-to-1 enum mapping test */
enum class SimpleEnumTarget { A, B, C }

/** Used for testing enum mapping with custom constant names */
enum class RenameEnumSource { PENDING_APPROVAL, USER_ACTIVE }

/** Used for testing enum mapping with custom constant names */
enum class RenameEnumTarget { PENDING, ACTIVE }

/** Used to test runtime failure for unmapped enum constants */
enum class UnmappedEnumSource { MAPPED, UNMAPPED }

/** Used to test runtime failure for unmapped enum constants */
enum class UnmappedEnumTarget { MAPPED }

/** Enum for testing default value assignment */
enum class Priority {
    LOW, MEDIUM, HIGH
}

/** Used for testing default enum value mapping */
data class EnumDefaultValueSource(val id: Int)

/** Used for testing default enum value mapping */
data class EnumDefaultValueTarget(val id: Int, val priority: Priority)
