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

package io.github.jacksever.automapper.processor.builder

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import io.github.jacksever.automapper.annotation.PropertyMapping
import kotlin.math.log

/**
 * Factory for creating [MapperBuilder] instances based on the source and target types
 *
 * This factory inspects the KSP symbols of the source and target classes to decide which
 * generation strategy is appropriate (Enum, Sealed, or Data class)
 */
internal object MapperBuilderFactory {

    /**
     * Returns an appropriate [MapperBuilder] for the given source and target classes
     *
     * - Returns [EnumMapperBuilder] if both source and target are Enums
     * - Returns [SealedMapperBuilder] if both source and target are Sealed classes/interfaces
     * - Returns [DataMapperBuilder] otherwise (default strategy for Data classes)
     *
     * @param logger logger for reporting information or warnings during builder creation
     * @param source source class declaration
     * @param target target class declaration
     * @param propertyMappings list of custom property mappings
     * @return Concrete implementation of [MapperBuilder]
     */
    fun getMapperBuilder(
        logger: KSPLogger,
        source: KSClassDeclaration,
        target: KSClassDeclaration,
        propertyMappings: List<PropertyMapping>,
    ): MapperBuilder {
        val isSourceEnum = source.classKind == ClassKind.ENUM_CLASS
        val isTargetEnum = target.classKind == ClassKind.ENUM_CLASS

        val isSourceSealed = source.modifiers.contains(Modifier.SEALED)
        val isTargetSealed = target.modifiers.contains(Modifier.SEALED)

        return when {
            isSourceEnum && isTargetEnum -> EnumMapperBuilder(logger = logger)
            isSourceSealed && isTargetSealed -> SealedMapperBuilder(logger = logger)
            else -> DataMapperBuilder(propertyMappings = propertyMappings)
        }
    }
}
