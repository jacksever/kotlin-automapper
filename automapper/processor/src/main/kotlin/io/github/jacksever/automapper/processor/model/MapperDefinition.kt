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
import io.github.jacksever.automapper.annotation.DefaultValue
import io.github.jacksever.automapper.annotation.PropertyMapping

/**
 * A data class that encapsulates all the necessary information for generating a single mapper
 *
 * @property source source [KSClassDeclaration]
 * @property target target [KSClassDeclaration]
 * @property reversible whether a reverse mapping should also be generated
 * @property defaultValues list of [DefaultValue] annotations for providing default values
 * @property converters list of [ConverterDefinition] for this specific mapping
 * @property propertyMappings list of [PropertyMapping] annotations for renaming properties
 */
internal data class MapperDefinition(
    val source: KSClassDeclaration,
    val target: KSClassDeclaration,
    val reversible: Boolean,
    val defaultValues: List<DefaultValue>,
    val converters: List<ConverterDefinition>,
    val propertyMappings: List<PropertyMapping>,
)
