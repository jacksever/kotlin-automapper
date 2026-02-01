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

package io.github.jacksever.automapper.processor.collector

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import io.github.jacksever.automapper.annotation.DefaultValue
import io.github.jacksever.automapper.annotation.DefaultValueSource
import io.github.jacksever.automapper.annotation.PropertyMapping
import io.github.jacksever.automapper.processor.extenstion.booleanArg
import io.github.jacksever.automapper.processor.extenstion.enumArg
import io.github.jacksever.automapper.processor.extenstion.getAnnotations
import io.github.jacksever.automapper.processor.extenstion.stringArg
import io.github.jacksever.automapper.processor.model.ConverterDefinition
import io.github.jacksever.automapper.processor.model.MapperDefinition

/**
 * A common interface for collecting Mapper Definitions
 */
internal interface MapperDefinitionCollector {

    /**
     * Collects all the necessary info to create a mapper from a function declaration
     *
     * @param function function declaration to process
     * @param mapperAnnotation specific `@AutoMapper` annotation instance
     * @param converters list of custom converter functions available for this mapping
     * @return Valid [MapperDefinition] or null if validation fails
     */
    fun collectMapperDefinition(
        function: KSFunctionDeclaration,
        mapperAnnotation: KSAnnotation,
        converters: List<ConverterDefinition>,
    ): MapperDefinition?
}

/**
 * This class encapsulates the logic for parsing a `@AutoMapper` annotation and its
 * containing function into a [MapperDefinition]
 */
internal class MapperDefinitionCollectorImpl(
    private val logger: KSPLogger,
) : MapperDefinitionCollector {

    override fun collectMapperDefinition(
        function: KSFunctionDeclaration,
        mapperAnnotation: KSAnnotation,
        converters: List<ConverterDefinition>,
    ): MapperDefinition? = runCatching {
        val parameters = function.parameters
        val functionName = function.simpleName.asString()
        val reversible = mapperAnnotation.booleanArg(name = "reversible", default = true)
        val propertyMappings = mapperAnnotation.getAnnotations(name = "propertyMappings")
            .map { annotation ->
                PropertyMapping(
                    from = annotation.stringArg(name = "from"),
                    to = annotation.stringArg(name = "to"),
                )
            }
            .toList()
        val defaultValues = mapperAnnotation.getAnnotations(name = "defaultValues")
            .map { annotation ->
                DefaultValue(
                    property = annotation.stringArg(name = "property"),
                    value = annotation.stringArg(name = "value"),
                    source = annotation.enumArg<DefaultValueSource>(
                        name = "source",
                        default = DefaultValueSource.INLINE,
                    ),
                )
            }
            .toList()

        check(parameters.size == 1) {
            "Function '$functionName' annotated with @AutoMapper must have exactly one parameter representing the source object"
        }

        val sourceParam = parameters.first()
        val sourceType = sourceParam.type.resolve()
        val targetType = requireNotNull(function.returnType?.resolve()) {
            "Function '$functionName' annotated with @AutoMapper must declare a return type representing the target object"
        }
        val sourceClass = requireNotNull(sourceType.declaration as? KSClassDeclaration) {
            "Source type '$sourceType' in function '$functionName' must be a class"
        }
        val targetClass = requireNotNull(targetType.declaration as? KSClassDeclaration) {
            "Target type '$targetType' in function '$functionName' must be a class"
        }

        MapperDefinition(
            source = sourceClass,
            target = targetClass,
            converters = converters,
            reversible = reversible,
            defaultValues = defaultValues,
            propertyMappings = propertyMappings,
        )
    }.onFailure { throwable ->
        logger.error(
            message = "MapperDefinitionCollector: Failed to process mapper function '${function.simpleName.asString()}': ${throwable.message}",
            symbol = function
        )
    }.getOrNull()
}
