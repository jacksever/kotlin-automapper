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

package io.github.jacksever.automapper.sample.converter

import io.github.jacksever.automapper.annotation.AutoConverter
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal object InstantConverter {

    @AutoConverter
    fun fromInstant(value: Instant): Long = value.toEpochMilliseconds()

    @AutoConverter
    fun toInstant(value: Long): Instant = Instant.fromEpochMilliseconds(epochMilliseconds = value)
}

@OptIn(ExperimentalTime::class)
internal object GlobalPriorityConverter {

    @AutoConverter
    fun fromInstant(value: Instant): Long = 111L

    @AutoConverter
    fun toInstant(value: Long): Instant = Instant.fromEpochMilliseconds(epochMilliseconds = 111L)
}

@OptIn(ExperimentalTime::class)
internal object LocalPriorityConverter {

    @AutoConverter
    fun fromInstant(value: Instant): Long = 999L

    @AutoConverter
    fun toInstant(value: Long): Instant = Instant.fromEpochMilliseconds(epochMilliseconds = 999L)
}
