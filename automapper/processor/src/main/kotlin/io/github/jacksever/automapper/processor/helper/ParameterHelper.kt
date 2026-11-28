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
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import io.github.jacksever.automapper.annotation.DefaultValue
import io.github.jacksever.automapper.annotation.DefaultValueSource.INLINE
import io.github.jacksever.automapper.annotation.DefaultValueSource.PARAMETER
import io.github.jacksever.automapper.annotation.DefaultValueSource.PARAMETER_WITH_DEFAULT
import io.github.jacksever.automapper.annotation.PropertyMapping
import io.github.jacksever.automapper.processor.converter.CollectionConverter
import io.github.jacksever.automapper.processor.converter.ObjectConverter
import io.github.jacksever.automapper.processor.converter.PrimitiveConverter
import io.github.jacksever.automapper.processor.exception.MappingException
import io.github.jacksever.automapper.processor.exception.RecursiveMappingException
import io.github.jacksever.automapper.processor.extension.isAssignableConsideringNullability
import io.github.jacksever.automapper.processor.formatter.DefaultValueFormatter.format
import io.github.jacksever.automapper.processor.logger.FailableMappingLogger
import io.github.jacksever.automapper.processor.model.ConverterDefinition
import io.github.jacksever.automapper.processor.model.MappingContext
import io.github.jacksever.automapper.processor.model.MappingType
import io.github.jacksever.automapper.processor.model.ResolvedConverter

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
     * @param source resolved source type
     * @param target resolved target type
     * @param context shared type scope and converter usage for this generated function
     * @param defaultValues list of default value mappings
     * @param converters list of custom converter functions available for this mapping
     * @param propertyMappings list of custom property mappings
     * @return List of [CodeBlock]s representing constructor arguments (e.g., `id = source.id`)
     */
    fun buildConstructorParameters(
        logger: KSPLogger,
        source: MappingType,
        target: MappingType,
        context: MappingContext,
        defaultValues: List<DefaultValue> = emptyList(),
        converters: List<ConverterDefinition> = emptyList(),
        propertyMappings: List<PropertyMapping> = emptyList(),
    ): List<CodeBlock> {
        val targetConstructor = checkNotNull(value = target.declaration.primaryConstructor) {
            "Target class ${target.declaration.simpleName.asString()} must have a primary constructor"
        }
        val sourceProperties = source.declaration.getAllProperties()
            .associateBy { property -> property.simpleName.asString() }
        val defaultValuesByName = defaultValues.associateBy { value -> value.property }

        return targetConstructor.parameters.mapNotNull { targetParameter ->
            val targetParameterName = checkNotNull(value = targetParameter.name?.asString()) {
                "Unnamed constructor parameter in ${target.declaration.simpleName.asString()}"
            }
            val mapping =
                propertyMappings.firstOrNull { mapping -> mapping.to == targetParameterName }
            val sourceParameterName = mapping?.from ?: targetParameterName
            val sourceProperty = sourceProperties[sourceParameterName]
            val defaultValue = defaultValuesByName[targetParameterName]
            val targetType = target.parameterType(parameter = targetParameter)

            when {
                sourceProperty != null -> {
                    val sourceType = source.propertyType(property = sourceProperty)
                    val converter = findConverter(
                        logger = logger,
                        to = targetType,
                        from = sourceType,
                        context = context,
                        converters = converters,
                    )
                    val isShadowed = defaultValue != null && defaultValue.source != INLINE &&
                            targetParameterName == sourceParameterName

                    if (converter != null) {
                        buildConverterAssignment(
                            converter = converter,
                            sourceType = sourceType,
                            targetType = targetType,
                            isShadowed = isShadowed,
                            defaultValue = defaultValue,
                            sourceParameterName = sourceParameterName,
                            targetParameterName = targetParameterName,
                        )
                    } else {
                        val sourceName = CodeBlock.of(
                            if (isShadowed) "this.%N" else "%N",
                            sourceParameterName,
                        )
                        val conversion = getConversionExpression(
                            logger = logger,
                            context = context,
                            sourceType = sourceType,
                            targetType = targetType,
                            converters = converters,
                            defaultValue = defaultValue,
                        )

                        CodeBlock.of("%N = %L%L", targetParameterName, sourceName, conversion)
                    }
                }

                defaultValue != null -> CodeBlock.of(
                    "%N = %L",
                    targetParameterName,
                    defaultValue.toExpression(targetType = targetType),
                )

                targetParameter.hasDefault -> null
                targetType.isMarkedNullable -> CodeBlock.of("%N = null", targetParameterName)

                else -> buildRecursiveAssignment(
                    logger = logger,
                    source = source,
                    target = target,
                    context = context,
                    targetType = targetType,
                    converters = converters,
                    targetParameter = targetParameter,
                )
            }
        }
    }

    /**
     * Constructs a missing nested property from the current source's properties
     *
     * A separate logger retains the nested error details while the outer diagnostic identifies
     * the target property. The resolved nested type is shared by parameter and constructor generation
     */
    private fun buildRecursiveAssignment(
        logger: KSPLogger,
        targetType: KSType,
        source: MappingType,
        target: MappingType,
        context: MappingContext,
        targetParameter: KSValueParameter,
        converters: List<ConverterDefinition>,
    ): CodeBlock {
        val propertyName = checkNotNull(value = targetParameter.name).asString()
        val targetClass = targetType.declaration as? KSClassDeclaration

        if (!targetClass.isEligibleForRecursiveMapping()) {
            val message = buildString {
                append("Cannot map property '$propertyName' for ${target.declaration.qualifiedName?.asString()}. ")
                append("No matching property found in ${source.declaration.qualifiedName?.asString()} and no default value is provided")
            }

            logger.error(message = message, symbol = targetParameter)
            throw MappingException(message = message)
        }

        return runCatching {
            val nestedTarget = context.types.resolve(type = targetType)
            val parameters = buildConstructorParameters(
                source = source,
                context = context,
                target = nestedTarget,
                converters = converters,
                logger = FailableMappingLogger(delegate = logger),
            )

            buildCodeBlock {
                add(
                    "%N = %T(\n",
                    propertyName,
                    nestedTarget.constructorType.toTypeName(typeParamResolver = context.typeParameters),
                )
                indent()
                parameters
                    .filter { expression -> expression.isNotEmpty() }
                    .forEach { parameter -> addStatement("%L,", parameter) }
                unindent()
                add(")")
            }
        }.getOrElse { throwable ->
            val message = buildString {
                append("Cannot map property '$propertyName' for ${target.declaration.qualifiedName?.asString()}. ")
                when (throwable) {
                    is RecursiveMappingException -> append("Recursive mapping failed. See the previous error for details")
                    else -> append("Recursive mapping failed: ${throwable.message}")
                }
            }

            logger.error(message = message, symbol = targetParameter)
            throw MappingException(message = message, cause = throwable)
        }
    }

    /**
     * Resolves an inline fallback or a reference to a runtime default parameter
     */
    private fun DefaultValue.toExpression(targetType: KSType): CodeBlock = when (source) {
        INLINE -> format(targetType = targetType, defaultValue = value)
        PARAMETER, PARAMETER_WITH_DEFAULT -> CodeBlock.of("%N", property)
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
        isShadowed: Boolean,
        sourceParameterName: String,
        targetParameterName: String,
        defaultValue: DefaultValue?,
        converter: ResolvedConverter,
    ): CodeBlock {
        val sourceName = CodeBlock.of(
            if (isShadowed) "this.%N" else "%N",
            sourceParameterName
        )
        val call = buildConverterCall(
            converter = converter,
            sourceType = sourceType,
            targetType = targetType,
            sourceName = sourceName,
            defaultValue = defaultValue,
        )

        return CodeBlock.of("%N = %L", targetParameterName, call)
    }

    /**
     * Builds a converter call with null handling for a property or collection element
     */
    private fun buildConverterCall(
        sourceType: KSType,
        targetType: KSType,
        sourceName: CodeBlock,
        defaultValue: DefaultValue?,
        converter: ResolvedConverter,
    ): CodeBlock {
        val member = MemberName(
            enclosingClassName = converter.definition.owner.toClassName(),
            simpleName = converter.definition.function.simpleName.asString(),
        )

        // Use ?.let if source is nullable but converter input is not
        val needsSafeCall = sourceType.isMarkedNullable && !converter.from.isMarkedNullable
        val baseCall = buildCodeBlock {
            if (needsSafeCall) {
                add("%L?.let { value -> %M(value) }", sourceName, member)
            } else {
                add("%M(%L)", member, sourceName)
            }
        }

        // The result of the base call is nullable if the original converter output was nullable,
        // OR if we introduced a safe call (`?.let`)
        val resultIsNullable = converter.to.isMarkedNullable || needsSafeCall

        // If the result is nullable but the target is not
        val needsFallback = resultIsNullable && !targetType.isMarkedNullable

        return buildCodeBlock {
            add(baseCall)

            if (needsFallback) {
                if (defaultValue != null) {
                    add(" ?: %L", defaultValue.toExpression(targetType = targetType))
                } else {
                    add("!!")
                }
            }
        }
    }

    /**
     * Builds the complete expression for converting an element named `value`.
     * An empty block means the element can be assigned without conversion.
     */
    private fun buildCollectionElementConversion(
        logger: KSPLogger,
        sourceType: KSType,
        targetType: KSType,
        context: MappingContext,
        converters: List<ConverterDefinition>,
    ): CodeBlock {
        val converter = findConverter(
            logger = logger,
            to = targetType,
            from = sourceType,
            context = context,
            converters = converters,
        )

        if (converter != null) {
            return buildConverterCall(
                defaultValue = null,
                converter = converter,
                sourceType = sourceType,
                targetType = targetType,
                sourceName = CodeBlock.of(format = "value"),
            )
        }

        val conversion = getConversionExpression(
            logger = logger,
            context = context,
            defaultValue = null,
            sourceType = sourceType,
            targetType = targetType,
            converters = converters,
        )

        return if (conversion.isEmpty()) conversion else CodeBlock.of("value%L", conversion)
    }

    /**
     * Determines the conversion expression needed to assign [sourceType] to [targetType]
     */
    private fun getConversionExpression(
        logger: KSPLogger,
        sourceType: KSType,
        targetType: KSType,
        context: MappingContext,
        converters: List<ConverterDefinition>,
        defaultValue: DefaultValue?,
    ): CodeBlock {
        // Exact collection types can still require element converters.
        if (sourceType == targetType && converters.isEmpty()) return CodeBlock.of("")

        val conversion = PrimitiveConverter.getConversion(from = sourceType, to = targetType)
            .takeIf { expression -> expression.isNotEmpty() }
            ?: CollectionConverter.getConversion(
                sourceType = sourceType,
                targetType = targetType,
                getInnerConversion = { sourceElementType, targetElementType ->
                    buildCollectionElementConversion(
                        logger = logger,
                        context = context,
                        converters = converters,
                        sourceType = sourceElementType,
                        targetType = targetElementType,
                    )
                },
            ).takeIf { expression -> expression.isNotEmpty() }
            ?: ObjectConverter.getConversion(
                sourceType = sourceType,
                targetType = targetType,
                types = context.types,
            )

        if (conversion.isEmpty() && !targetType.isAssignableConsideringNullability(from = sourceType)) {
            val message =
                "No conversion available from '$sourceType' to '$targetType'. Provide an @AutoConverter or a matching mapper"

            logger.error(message = message, symbol = sourceType.declaration)
            throw MappingException(message = message)
        }

        return when {
            !sourceType.isMarkedNullable -> conversion
            targetType.isMarkedNullable -> {
                if (conversion.isEmpty()) conversion else CodeBlock.of("?%L", conversion)
            }

            defaultValue == null -> CodeBlock.of("!!%L", conversion)

            else -> {
                val fallback = defaultValue.toExpression(targetType = targetType)

                if (conversion.isEmpty()) {
                    CodeBlock.of(" ?: %L", fallback)
                } else {
                    CodeBlock.of("?%L ?: %L", conversion, fallback)
                }
            }
        }
    }

    /**
     * Finds the most specific custom converter for the given [from] and [to] types
     *
     * This function filters all available [converters] to find candidates where the types are
     * compatible. If multiple converters match, it selects the most specific one based on
     * two criteria: nullability match and type specificity
     *
     * @return the best matching [ResolvedConverter], or `null` if no suitable converter is found
     */
    private fun findConverter(
        from: KSType,
        to: KSType,
        logger: KSPLogger,
        context: MappingContext,
        converters: List<ConverterDefinition>,
    ): ResolvedConverter? {
        val candidates = converters.mapNotNull { converter ->
            context.converterResolver.resolve(converter = converter, from = from, to = to)
        }

        if (candidates.isEmpty()) return null
        if (candidates.size == 1) {
            return candidates.first()
                .also { converter -> context.usedConverters += converter.definition }
        }

        // Prioritize converters that match the nullability of the 'from' parameter to avoid `?.let`
        val preferred = candidates
            .filter { definition -> definition.from.isMarkedNullable == from.isMarkedNullable }
            .takeIf { list -> list.isNotEmpty() }
            ?: candidates

        val comparator = Comparator<ResolvedConverter> { first, second ->
            when {
                first == second -> 0
                first.isMoreSpecificThan(other = second, context = context) -> 1
                second.isMoreSpecificThan(other = first, context = context) -> -1
                else -> 0
            }
        }
        val best = preferred.maxWith(comparator = comparator)
        val ambiguous = preferred.any { definition ->
            definition != best && !best.isMoreSpecificThan(other = definition, context = context)
        }

        val selected = if (ambiguous) {
            val names = preferred.joinToString { definition ->
                definition.definition.function.qualifiedName?.asString().orEmpty()
            }

            logger.warn(
                message = buildString {
                    append("Ambiguous converters found for mapping $from -> $to. ")
                    append("Candidates: $names. ")
                    append("Using the first one found: ${preferred.first().definition.function.qualifiedName?.asString()}")
                }
            )

            preferred.first()
        } else {
            best
        }

        context.usedConverters += selected.definition

        return selected
    }

    /**
     * Compares instantiated signatures, then their original generic constraints when types match
     */
    private fun ResolvedConverter.isMoreSpecificThan(
        context: MappingContext,
        other: ResolvedConverter,
    ): Boolean {
        if (!other.from.isAssignableFrom(that = from) || !other.to.isAssignableFrom(that = to)) return false
        if (from != other.from || to != other.to) return true

        return context.converterResolver.isMoreSpecific(
            first = definition,
            second = other.definition,
        )
    }

    /**
     * Checks if a class is eligible for recursive mapping
     */
    private fun KSClassDeclaration?.isEligibleForRecursiveMapping(): Boolean =
        this != null && primaryConstructor != null && classKind == ClassKind.CLASS &&
                !packageName.asString().startsWith(prefix = "kotlin")
}
