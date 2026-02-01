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

package io.github.jacksever.automapper.processor.helper

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import io.github.jacksever.automapper.annotation.DefaultValue
import io.github.jacksever.automapper.annotation.PropertyMapping
import io.github.jacksever.automapper.processor.converter.CollectionConverter
import io.github.jacksever.automapper.processor.converter.ObjectConverter
import io.github.jacksever.automapper.processor.converter.PrimitiveConverter
import io.github.jacksever.automapper.processor.extenstion.asMemberName
import io.github.jacksever.automapper.processor.extenstion.isAssignableConsideringNullability
import io.github.jacksever.automapper.processor.formatter.DefaultValueFormatter.format
import io.github.jacksever.automapper.processor.model.ConverterDefinition

/**
 * Helper object for generating constructor parameters and handling type conversions
 *
 * This object provides utility methods to match properties between source and target classes
 * and generate the appropriate assignment code, including type conversions if necessary
 */
internal object ParameterHelper {

    /**
     * Builds a list of constructor parameter assignments as [CodeBlock]s for the target class
     * based on properties from the source class, applying custom mappings
     *
     * @param logger KSP logger
     * @param sourceClass source class declaration
     * @param targetClass target class declaration
     * @param defaultValues list of default value mappings
     * @param converters list of custom converter functions available for this mapping
     * @param propertyMappings list of custom property mappings
     * @return List of [CodeBlock]s representing constructor arguments (e.g., `id = source.id`)
     */
    fun buildConstructorParameters(
        logger: KSPLogger,
        sourceClass: KSClassDeclaration,
        targetClass: KSClassDeclaration,
        defaultValues: List<DefaultValue> = emptyList(),
        converters: List<ConverterDefinition> = emptyList(),
        propertyMappings: List<PropertyMapping> = emptyList(),
    ): List<CodeBlock> = buildList {
        val targetConstructor = targetClass.primaryConstructor
        val sourceProperties = sourceClass.getAllProperties()
            .associateBy { property -> property.simpleName.asString() }
        val defaultValuesByName = defaultValues.associateBy { value -> value.property }

        if (targetConstructor == null) {
            logger.error(
                message = "Target class ${targetClass.simpleName.asString()} must have a primary constructor",
                symbol = targetClass
            )
            return@buildList
        }

        targetConstructor.parameters.forEach { targetParameter ->
            val targetParameterName = targetParameter.name?.asString() ?: run {
                logger.error(
                    message = "Unnamed constructor parameter in ${targetClass.simpleName.asString()}",
                    symbol = targetParameter
                )
                return@forEach
            }

            val mapping =
                propertyMappings.firstOrNull { mapping -> mapping.to == targetParameterName }
            val sourceParameterName = mapping?.from ?: targetParameterName
            val sourceProperty = sourceProperties[sourceParameterName]
            val defaultValue = defaultValuesByName[targetParameterName]

            when {
                sourceProperty != null -> {
                    val sourceType = sourceProperty.type.resolve()
                    val targetType = targetParameter.type.resolve()

                    // Explicit custom converter has highest priority
                    val explicitConverter = findConverter(
                        logger = logger,
                        to = targetType,
                        from = sourceType,
                        converters = converters,
                    )

                    if (explicitConverter != null) {
                        add(
                            buildConverterAssignment(
                                sourceType = sourceType,
                                targetType = targetType,
                                defaultValue = defaultValue,
                                converter = explicitConverter,
                                sourceParameterName = sourceParameterName,
                                targetParameterName = targetParameterName,
                            )
                        )
                    } else {
                        // Built-in conversions
                        val conversion = getConversionExpression(
                            sourceType = sourceType,
                            targetType = targetType,
                            defaultValue = defaultValue,
                        )

                        add(
                            buildCodeBlock {
                                add(
                                    "%L = %L%L",
                                    targetParameterName,
                                    sourceParameterName,
                                    conversion
                                )
                            }
                        )
                    }
                }

                defaultValue != null -> {
                    val targetType = targetParameter.type.resolve()
                    val formattedValue =
                        format(targetType = targetType, defaultValue = defaultValue.value)

                    add(buildCodeBlock { add("%L = %L", targetParameterName, formattedValue) })
                }

                targetParameter.hasDefault -> {
                    /* Do nothing, let the compiler use the default value */
                }

                targetParameter.type.resolve().isMarkedNullable -> {
                    add(buildCodeBlock { add("%L = null", targetParameterName) })
                }

                else -> {
                    logger.error(
                        message = buildString {
                            append("Cannot map property '$targetParameterName' for ${targetClass.qualifiedName?.asString()}. ")
                            append("No matching property found in ${sourceClass.qualifiedName?.asString()} and no default value is provided")
                        },
                        symbol = targetParameter
                    )
                }
            }
        }
    }

    /**
     * Builds a [CodeBlock] for a custom converter assignment statement
     *
     * This function generates the necessary code to call a custom converter and assign it
     * to the target property. It handles all combinations of nullability for the source property,
     * target property, and the converter's input and output types
     */
    private fun buildConverterAssignment(
        sourceType: KSType,
        targetType: KSType,
        sourceParameterName: String,
        targetParameterName: String,
        defaultValue: DefaultValue?,
        converter: ConverterDefinition,
    ): CodeBlock {
        val member = converter.function.asMemberName()

        // Use ?.let if source is nullable but converter input is not
        val needsSafeCall = sourceType.isMarkedNullable && !converter.from.isMarkedNullable
        val baseCall = buildCodeBlock {
            if (needsSafeCall) {
                add("%L?.let { value -> %M(value) }", sourceParameterName, member)
            } else {
                add("%M(%L)", member, sourceParameterName)
            }
        }

        // The result of the base call is nullable if the original converter output was nullable,
        // OR if we introduced a safe call (`?.let`)
        val resultIsNullable = converter.to.isMarkedNullable || needsSafeCall

        // If the result is nullable but the target is not
        val needsFallback = resultIsNullable && !targetType.isMarkedNullable

        return buildCodeBlock {
            add("%L = ", targetParameterName)
            add(baseCall)

            if (needsFallback) {
                defaultValue?.let { value ->
                    add(" ?: %L", format(targetType = targetType, defaultValue = value.value))
                } ?: add("!!")
            }
        }
    }

    /**
     * Determines the conversion expression needed to assign [sourceType] to [targetType]
     */
    private fun getConversionExpression(
        sourceType: KSType,
        targetType: KSType,
        defaultValue: DefaultValue?,
    ): CodeBlock {
        // 0. Exact match
        if (sourceType == targetType) return CodeBlock.of("")

        // 1. Try Primitive Conversion
        var conversion = PrimitiveConverter.getConversion(from = sourceType, to = targetType)

        // 2. Try Collection Conversion
        if (conversion.isEmpty()) {
            conversion = CollectionConverter.getConversion(
                sourceType = sourceType,
                targetType = targetType,
                defaultValue = defaultValue,
                getInnerConversion = ::getConversionExpression,
            )
        }

        // 3. Try Object Conversion
        if (conversion.isEmpty()) {
            conversion = ObjectConverter.getConversion(
                sourceType = sourceType,
                targetType = targetType,
            )
        }

        // 4. Handle Nullability
        if (sourceType.isMarkedNullable) {
            if (!targetType.isMarkedNullable) {
                return buildCodeBlock {
                    defaultValue?.let { default ->
                        add(
                            "%L ?: %L",
                            conversion,
                            format(targetType = targetType, defaultValue = default.value)
                        )
                    } ?: add("!!%L", conversion)
                }
            } else {
                if (conversion.isNotEmpty()) {
                    return buildCodeBlock { add("?%L", conversion) }
                }
            }
        }

        return conversion
    }

    /**
     * Finds the most specific custom converter for the given [from] and [to] types
     *
     * This function filters all available [converters] to find candidates where the types are
     * compatible. If multiple converters match, it selects the most specific one based on
     * two criteria: nullability match and type specificity
     *
     * @return the best matching [ConverterDefinition], or `null` if no suitable converter is found
     */
    private fun findConverter(
        logger: KSPLogger,
        from: KSType,
        to: KSType,
        converters: List<ConverterDefinition>,
    ): ConverterDefinition? {
        val candidates = converters.filter { converter ->
            converter.from.isAssignableConsideringNullability(from = from) &&
                    to.isAssignableConsideringNullability(from = converter.to)
        }

        if (candidates.isEmpty()) return null
        if (candidates.size == 1) return candidates.first()

        // Prioritize converters that match the nullability of the 'from' parameter to avoid `?.let`
        val preferred = candidates
            .filter { definition -> definition.from.isMarkedNullable == from.isMarkedNullable }
            .takeIf { list -> list.isNotEmpty() }
            ?: candidates

        val best = preferred.maxWith(comparator = specificityComparator)
        val ambiguous = preferred.any { definition ->
            definition != best && !definition.isMoreSpecificThan(other = best)
        }

        return if (ambiguous) {
            val names = preferred.joinToString { definition ->
                definition.function.qualifiedName?.asString().orEmpty()
            }
            logger.warn(
                message = buildString {
                    append("Ambiguous converters found for mapping $from -> $to. ")
                    append("Candidates: $names. ")
                    append("Using the first one found: ${preferred.first().function.qualifiedName?.asString()}")
                }
            )

            preferred.first()
        } else {
            best
        }
    }

    /**
     * A comparator for sorting ConverterDefinition instances by specificity
     */
    private val specificityComparator = Comparator<ConverterDefinition> { a, b ->
        when {
            a == b -> 0
            a.isMoreSpecificThan(other = b) -> 1
            b.isMoreSpecificThan(other = a) -> -1
            else -> 0
        }
    }

    /**
     * Determines if this [ConverterDefinition] is more specific than another one
     *
     * A converter is more specific if its `from` type is a subtype of the other's `from` type,
     * and its `to` type is a subtype of the other's `to` type
     */
    private fun ConverterDefinition.isMoreSpecificThan(other: ConverterDefinition): Boolean =
        other.from.isAssignableFrom(that = from) && other.to.isAssignableFrom(that = to)
}
