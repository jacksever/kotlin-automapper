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
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toClassName
import io.github.jacksever.automapper.annotation.DefaultValue
import io.github.jacksever.automapper.annotation.PropertyMapping
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
                    val customConverter = findConverter(
                        to = targetType,
                        from = sourceType,
                        converters = converters,
                    )

                    if (customConverter != null) {
                        val converterBlock = buildConverterAssignment(
                            sourceType = sourceType,
                            targetType = targetType,
                            converter = customConverter,
                            defaultValue = defaultValue,
                            sourceParameterName = sourceParameterName,
                            targetParameterName = targetParameterName,
                        )

                        add(converterBlock)
                    } else {
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
                    val formattedValue = formatDefaultValue(targetType, defaultValue.value)

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
     * to the target property. It handles nullability of the source and target types,
     * and applies a default value if provided
     */
    private fun buildConverterAssignment(
        sourceType: KSType,
        targetType: KSType,
        sourceParameterName: String,
        targetParameterName: String,
        defaultValue: DefaultValue?,
        converter: ConverterDefinition,
    ): CodeBlock {
        val function = converter.function
        val member = if (function.parentDeclaration is KSClassDeclaration) {
            MemberName(
                simpleName = function.simpleName.asString(),
                enclosingClassName = (function.parentDeclaration as KSClassDeclaration).toClassName(),
            )
        } else {
            MemberName(
                simpleName = function.simpleName.asString(),
                packageName = function.packageName.asString(),
            )
        }

        return if (sourceType.isMarkedNullable) {
            when {
                !targetType.isMarkedNullable && defaultValue != null -> buildCodeBlock {
                    add(
                        "%L = %L?.let(%M) ?: %L",
                        targetParameterName,
                        sourceParameterName,
                        member,
                        formatDefaultValue(targetType, defaultValue.value)
                    )
                }

                !targetType.isMarkedNullable -> buildCodeBlock {
                    add("%L = %M(%L!!)", targetParameterName, member, sourceParameterName)
                }

                else -> buildCodeBlock {
                    add("%L = %L?.let(%M)", targetParameterName, sourceParameterName, member)
                }
            }
        } else {
            buildCodeBlock {
                add("%L = %M(%L)", targetParameterName, member, sourceParameterName)
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
        var conversion =
            getPrimitiveConversionExpression(sourceType = sourceType, targetType = targetType)

        // 2. Try Collection Conversion
        if (conversion.isEmpty()) {
            conversion = getCollectionConversion(
                sourceType = sourceType,
                targetType = targetType,
                defaultValue = defaultValue,
            )
        }

        // 3. Try Object Conversion
        if (conversion.isEmpty()) {
            conversion = getObjectConversion(sourceType = sourceType, targetType = targetType)
        }

        // 4. Handle Nullability
        if (sourceType.isMarkedNullable) {
            if (!targetType.isMarkedNullable) {
                return if (defaultValue == null) {
                    // No default value, use non-null assertion (potentially unsafe)
                    buildCodeBlock { add("!!%L", conversion) }
                } else {
                    // A default value is provided, use Elvis operator
                    buildCodeBlock {
                        add(
                            "%L ?: %L",
                            conversion,
                            formatDefaultValue(targetType, defaultValue.value)
                        )
                    }
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
     * Finds a custom converter function for the given [from] and [to] types
     */
    private fun findConverter(
        from: KSType,
        to: KSType,
        converters: List<ConverterDefinition>
    ): ConverterDefinition? = converters.find { converter ->
        converter.from.makeNotNullable() == from.makeNotNullable() && converter.to.makeNotNullable() == to.makeNotNullable()
    }

    /**
     * Attempts to generate a conversion expression for collection types (List, Set)
     *
     * Checks if both types are supported collections and recursively generates conversions
     * for their type arguments. Handles `List <-> Set` transformations
     *
     * @return conversion string (e.g. ".map { it.asTarget() }") or empty string if not a supported collection conversion
     */
    private fun getCollectionConversion(
        sourceType: KSType,
        targetType: KSType,
        defaultValue: DefaultValue?,
    ): CodeBlock {
        val sourceDeclaration =
            sourceType.declaration as? KSClassDeclaration ?: return CodeBlock.of("")
        val targetDeclaration =
            targetType.declaration as? KSClassDeclaration ?: return CodeBlock.of("")

        val isSourceList = sourceDeclaration.isList()
        val isSourceSet = sourceDeclaration.isSet()
        val isTargetList = targetDeclaration.isList()
        val isTargetSet = targetDeclaration.isSet()

        // Check if both are supported collections
        if ((isSourceList || isSourceSet) && (isTargetList || isTargetSet)) {
            val sourceArg = sourceType.arguments.firstOrNull()?.type?.resolve()
            val targetArg = targetType.arguments.firstOrNull()?.type?.resolve()

            if (sourceArg != null && targetArg != null) {
                val innerConversion = getConversionExpression(
                    sourceType = sourceArg,
                    targetType = targetArg,
                    defaultValue = defaultValue,
                )

                // If elements need conversion OR container type changes (e.g. Set -> List)
                if (innerConversion.isNotEmpty() || (isSourceSet && isTargetList) || (isSourceList && isTargetSet)) {
                    return buildCodeBlock {
                        add(".map { value -> value%L }", innerConversion)
                        if (isTargetSet) {
                            add(".toSet()")
                        }
                    }
                }
            }
        }

        return CodeBlock.of("")
    }

    /**
     * Attempts to generate a recursive mapping call for object types
     *
     * Used when mapping nested objects (e.g. `User.address` -> `UserEntity.address`).
     * It assumes an extension function `asTarget()` exists for the source type if they are custom classes
     *
     * @param sourceType type of the source property
     * @param targetType type of the target property
     * @return conversion string (e.g. ".asTarget()") or empty string
     */
    private fun getObjectConversion(sourceType: KSType, targetType: KSType): CodeBlock {
        val sourceDeclaration = sourceType.declaration
        val targetDeclaration = targetType.declaration

        if (sourceDeclaration is KSClassDeclaration && targetDeclaration is KSClassDeclaration) {
            val sourcePkg = sourceDeclaration.packageName.asString()
            val targetPkg = targetDeclaration.packageName.asString()

            if (!sourcePkg.startsWith(prefix = "kotlin") && !targetPkg.startsWith(prefix = "kotlin")) {
                return CodeBlock.of(".as${targetDeclaration.simpleName.asString()}()")
            }
        }

        return CodeBlock.of("")
    }

    /**
     * Generates a conversion expression for standard primitive types and Strings
     *
     * For example, it might return a [CodeBlock] containing ".toInt()"
     */
    private fun getPrimitiveConversionExpression(
        sourceType: KSType,
        targetType: KSType,
    ): CodeBlock {
        val sourceTypeName = sourceType.declaration.simpleName.asString()
        val targetTypeName = targetType.declaration.simpleName.asString()

        val conversion = when (sourceTypeName) {
            "String" -> when (targetTypeName) {
                "Long" -> ".toLong()"
                "Int" -> ".toInt()"
                "Double" -> ".toDouble()"
                "Float" -> ".toFloat()"
                "Boolean" -> ".toBoolean()"
                else -> ""
            }

            "Int" -> if (targetTypeName == "Long") ".toLong()" else ".toString()"
            "Long" -> if (targetTypeName == "Int") ".toInt()" else ".toString()"
            "Double" -> if (targetTypeName == "Float") ".toFloat()" else ".toString()"
            "Float" -> if (targetTypeName == "Double") ".toDouble()" else ".toString()"
            "Boolean" -> if (targetTypeName == "String") ".toString()" else ""
            else -> ""
        }

        return CodeBlock.of(conversion)
    }

    /**
     * Formats the default value string into a [CodeBlock] based on the target type
     */
    private fun formatDefaultValue(targetType: KSType, defaultValue: String): CodeBlock {
        val targetTypeName = targetType.declaration.simpleName.asString()

        return when {
            targetTypeName == "String" -> CodeBlock.of("%S", defaultValue)
            targetTypeName == "Char" -> CodeBlock.of("'%L'", defaultValue.trim('\''))
            targetType.isEnum() || targetType.isSealed() -> {
                val entry = defaultValue.substringAfterLast(delimiter = '.')
                val className = (targetType.declaration as KSClassDeclaration).toClassName()

                CodeBlock.of("%T.%L", className, entry)
            }

            else -> CodeBlock.of("%L", defaultValue)
        }
    }

    /**
     * Checks if the [KSType] represents an enum class
     */
    private fun KSType.isEnum(): Boolean =
        (declaration as? KSClassDeclaration)?.classKind == ClassKind.ENUM_CLASS

    /**
     * Checks if the [KSType] represents a sealed class
     */
    private fun KSType.isSealed(): Boolean = declaration.modifiers.contains(Modifier.SEALED)

    /**
     * Checks if the class declaration corresponds to a List type (Kotlin or Java)
     */
    private fun KSClassDeclaration.isList(): Boolean {
        val name = qualifiedName?.asString()

        return name == "kotlin.collections.List" ||
                name == "kotlin.collections.MutableList" ||
                name == "java.util.List"
    }

    /**
     * Checks if the class declaration corresponds to a Set type (Kotlin or Java)
     */
    private fun KSClassDeclaration.isSet(): Boolean {
        val name = qualifiedName?.asString()

        return name == "kotlin.collections.Set" ||
                name == "kotlin.collections.MutableSet" ||
                name == "java.util.Set"
    }
}
