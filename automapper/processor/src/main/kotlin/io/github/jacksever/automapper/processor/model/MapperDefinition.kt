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

import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.github.jacksever.automapper.annotation.PropertyMapping

/**
 * Represents a defined mapping between two classes
 *
 * This data class holds the metadata required to generate a mapper function,
 * extracted from the `@AutoMapper` annotation and the function signature
 *
 * @property source source class declaration (input type)
 * @property target target class declaration (output/return type)
 * @property reversible flag indicating whether a reverse mapping (Target -> Source) should also be generated
 * @property propertyMappings list of [PropertyMapping] rules for this specific mapping
 */
internal data class MapperDefinition(
    val source: KSClassDeclaration,
    val target: KSClassDeclaration,
    val reversible: Boolean,
    val propertyMappings: List<PropertyMapping>,
)
