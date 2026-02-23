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
import io.github.jacksever.automapper.annotation.AutoConverter
import io.github.jacksever.automapper.processor.extension.getArgument
import io.github.jacksever.automapper.processor.model.ConverterDefinition

/**
 * A common interface for collecting converters from annotations
 */
internal interface ConverterCollector {

    /**
     * Parses a list of converter classes from an `@AutoMapperModule` or `@AutoMapper` annotation
     *
     * @param annotation annotation instance to process
     * @return List of [ConverterDefinition]s
     */
    fun collectConverters(annotation: KSAnnotation): List<ConverterDefinition>
}

/**
 * This class encapsulates the logic for finding all converters from an annotation
 */
internal class ConverterCollectorImpl(
    private val logger: KSPLogger,
) : ConverterCollector {

    override fun collectConverters(
        annotation: KSAnnotation
    ): List<ConverterDefinition> {
        val converterTypes = (annotation.getArgument(name = "converters") as? List<*>)
            ?.filterIsInstance<KSType>()
            .orEmpty()

        if (converterTypes.isEmpty()) return emptyList()

        logger.info(message = "Found ${converterTypes.size} converter classes in annotation")

        return converterTypes
            .asSequence()
            .map(transform = KSType::declaration)
            .filterIsInstance<KSClassDeclaration>()
            .flatMap(transform = ::collectFromConverterClass)
            .toList()
    }

    /**
     * Scans a given [KSClassDeclaration] for functions annotated with `@AutoConverter`
     */
    private fun collectFromConverterClass(converterClass: KSClassDeclaration): Sequence<ConverterDefinition> =
        converterClass
            .getAllFunctions()
            .mapNotNull { function ->
                if (!function.hasAutoConverterAnnotation()) return@mapNotNull null

                validateAndBuildConverter(function = function)
            }

    /**
     * Validates a function annotated with `@AutoConverter` and builds a [ConverterDefinition] if it's valid
     *
     * A valid converter function must:
     * 1. Have exactly one parameter (the source type)
     * 2. Have a non-Unit return type (the target type)
     */
    private fun validateAndBuildConverter(function: KSFunctionDeclaration): ConverterDefinition? {
        val params = function.parameters
        val returnType = function.returnType?.resolve()

        if (params.size != 1 || returnType == null) {
            logger.error(
                message = "Invalid @AutoConverter function '${function.qualifiedName?.asString()}': must have exactly one parameter and a non-Unit return type",
                symbol = function
            )

            return null
        }

        val fromType = params[0].type.resolve()

        logger.info(message = "Found converter: ${function.qualifiedName?.asString()} from ${fromType.declaration.simpleName.asString()} to ${returnType.declaration.simpleName.asString()}")

        return ConverterDefinition(from = fromType, to = returnType, function = function)
    }

    /**
     * Checks if a function is annotated with [AutoConverter]
     */
    private fun KSFunctionDeclaration.hasAutoConverterAnnotation(): Boolean =
        annotations.any { annotation ->
            annotation.shortName.asString() == AutoConverter::class.simpleName
        }
}
