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
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeParameter
import io.github.jacksever.automapper.annotation.DefaultValue
import io.github.jacksever.automapper.annotation.DefaultValueSource
import io.github.jacksever.automapper.annotation.PropertyMapping
import io.github.jacksever.automapper.processor.extension.booleanArg
import io.github.jacksever.automapper.processor.extension.enumArg
import io.github.jacksever.automapper.processor.extension.getAnnotations
import io.github.jacksever.automapper.processor.extension.stringArg
import io.github.jacksever.automapper.processor.model.ConverterDefinition
import io.github.jacksever.automapper.processor.model.MapperDefinition
import io.github.jacksever.automapper.processor.resolver.MappingTypeResolver

/**
 * A common interface for collecting Mapper Definitions
 */
internal interface MapperDefinitionCollector {

    /**
     * Collects all the necessary info to create a mapper from a function declaration
     *
     * @param types resolver for the current KSP round
     * @param function function declaration to process
     * @param mapperAnnotation specific `@AutoMapper` annotation instance
     * @param converters list of custom converter functions available for this mapping
     * @return Valid [MapperDefinition] or null if validation fails
     */
    fun collectMapperDefinition(
        types: MappingTypeResolver,
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
        types: MappingTypeResolver,
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

        check(value = parameters.size == 1) {
            "Function '$functionName' annotated with @AutoMapper must have exactly one parameter representing the source object"
        }

        val sourceParam = parameters.first()
        val sourceType = sourceParam.type.resolve()
        val targetType = requireNotNull(value = function.returnType?.resolve()) {
            "Function '$functionName' annotated with @AutoMapper must declare a return type representing the target object"
        }
        val source = types.resolve(type = sourceType)
        val target = types.resolve(type = targetType)
        check(value = !source.type.isMarkedNullable && !target.type.isMarkedNullable) {
            "Mapper '$functionName' must use non-nullable source and target types; property types may be nullable"
        }
        val typeParameters = collectTypeParameters(
            function = function,
            source = source.type,
            target = target.type,
        )

        MapperDefinition(
            source = source,
            target = target,
            function = function,
            converters = converters,
            reversible = reversible,
            defaultValues = defaultValues,
            typeParameters = typeParameters,
            propertyMappings = propertyMappings,
        )
    }.onFailure { throwable ->
        logger.error(
            message = "MapperDefinitionCollector: Failed to process mapper function '${function.simpleName.asString()}': ${throwable.message}",
            symbol = function
        )
    }.getOrNull()

    /**
     * Collects only variables referenced by the mapping signature or their upper bounds
     *
     * Unused enclosing module parameters must not become generated function parameters, because
     * callers would otherwise be unable to infer them from the receiver or result
     */
    private fun collectTypeParameters(
        source: KSType,
        target: KSType,
        function: KSFunctionDeclaration,
    ): List<KSTypeParameter> {
        val owners = generateSequence(seed = function.parentDeclaration) { declaration ->
            declaration.parentDeclaration
        }.toList().asReversed()
        val candidates = owners.filterIsInstance<KSClassDeclaration>()
            .flatMap { owner -> owner.typeParameters } + function.typeParameters
        val required = linkedSetOf<KSTypeParameter>()

        fun visit(type: KSType) {
            val parameter = type.declaration as? KSTypeParameter

            if (parameter != null && parameter in candidates && required.add(element = parameter)) {
                parameter.bounds.forEach { bound -> visit(type = bound.resolve()) }
            }

            type.arguments.forEach { argument ->
                argument.type?.resolve()?.let { argumentType -> visit(type = argumentType) }
            }
        }

        visit(type = source)
        visit(type = target)

        return candidates.filter { parameter -> parameter in required }.distinct()
    }
}
