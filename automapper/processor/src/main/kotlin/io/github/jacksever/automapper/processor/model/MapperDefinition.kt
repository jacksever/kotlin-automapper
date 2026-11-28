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

import com.google.devtools.ksp.symbol.KSTypeParameter
import io.github.jacksever.automapper.annotation.DefaultValue
import io.github.jacksever.automapper.annotation.PropertyMapping

/**
 * Data model representing a single mapping operation to be generated
 *
 * @property source full source type, including arguments
 * @property target full target type, including arguments
 * @property name original mapper method name, also used to distinguish JVM overloads
 * @property typeParameters type variables required by the mapping signature and their bounds
 * @property reversible if true, a reverse mapping function will also be generated
 * @property defaultValues list of default value mappings for target properties
 * @property converters list of custom converter functions available for this mapping
 * @property propertyMappings list of custom property mappings
 */
internal data class MapperDefinition(
    val source: MappingType,
    val target: MappingType,
    val name: String,
    val typeParameters: List<KSTypeParameter>,
    val reversible: Boolean,
    val defaultValues: List<DefaultValue>,
    val converters: List<ConverterDefinition>,
    val propertyMappings: List<PropertyMapping>,
) {

    /**
     * Builds the reverse mapping with the same generic scope and reversed property names
     *
     * Default values belong to the original target and are not applied to the reverse direction
     */
    fun reversed(): MapperDefinition = copy(
        source = target,
        target = source,
        reversible = false,
        defaultValues = emptyList(),
        propertyMappings = propertyMappings.map { mapping -> PropertyMapping(from = mapping.to, to = mapping.from) },
    )
}
