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

package io.github.jacksever.automapper.processor.model

/**
 * Describes one generated extension after overload validation
 *
 * The definition represents one mapping direction. Kotlin and JVM names are resolved together
 * so the function builder does not need to inspect other mappings in the file
 *
 * @property name Kotlin extension name
 * @property definition configuration for this mapping direction
 * @property jvmName distinct JVM name when generic overloads would otherwise share an erased signature
 */
internal data class MappingFunction(
    val name: String,
    val definition: MapperDefinition,
    val jvmName: String? = null,
)
