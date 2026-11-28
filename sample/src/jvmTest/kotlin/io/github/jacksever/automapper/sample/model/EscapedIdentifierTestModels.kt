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
import kotlin.uuid.Uuid

data class EscapedPropertySource(
    val `when`: Int?,
    val `source value`: String,
    val `object`: String?,
    val `is`: Instant?,
    val `as`: Uuid?,
)

data class EscapedPropertyTarget(
    val `when`: Long,
    val `target value`: String,
    val `object`: String,
    val `is`: Long,
    val `as`: String,
    val `class`: String,
)

data class EscapedDefaultSource(val id: Int)

data class EscapedDefaultTarget(
    val `object`: String,
    val `when`: EscapedEnumTarget,
    val `is`: String?,
    val `class`: EscapedNestedTarget,
)

data class EscapedNestedTarget(val id: Int)

enum class EscapedEnumSource { `when`, `class`, `else` }

enum class EscapedEnumTarget { `when`, `object` }
