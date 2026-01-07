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

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import io.github.jacksever.automapper.annotation.PropertyMapping

/**
 * Helper object for generating constructor parameters and handling type conversions
 *
 * This object provides utility methods to match properties between source and target classes
 * and generate the appropriate assignment code, including type conversions if necessary
 */
internal object ParameterHelper {

    /**
     * Builds a list of constructor parameter assignments for the target class
     * based on properties from the source class, applying custom mappings
     *
     * @param sourceClass source class declaration
     * @param targetClass target class declaration
     * @param propertyMappings list of custom property mappings
     * @return List of strings representing constructor arguments (e.g., "id = sourceId")
     */
    fun buildConstructorParameters(
        sourceClass: KSClassDeclaration,
        targetClass: KSClassDeclaration,
        propertyMappings: List<PropertyMapping> = emptyList(),
    ): List<String> = buildList {
        val sourceProperties = sourceClass.getAllProperties()
            .associateBy { property -> property.simpleName.asString() }
        val targetProperties = targetClass.getAllProperties().toList()

        targetProperties.forEach { targetProperty ->
            val targetParamName = targetProperty.simpleName.asString()

            // Find the mapping rule for the current target property
            val mapping = propertyMappings.firstOrNull { mapping ->
                mapping.to == targetParamName || (mapping.to.isEmpty() && mapping.from == targetParamName)
            }

            // Determine the source property name from the rule, or fallback to the target name
            val sourcePropName = mapping?.from ?: targetParamName

            sourceProperties[sourcePropName]?.let { sourceProperty ->
                val targetType = targetProperty.type.resolve()
                val sourceType = sourceProperty.type.resolve()
                val conversion = getConversionExpression(
                    sourceType = sourceType,
                    targetType = targetType,
                    defaultValue = mapping?.defaultValue,
                )

                add("$targetParamName = ${sourceProperty.simpleName.asString()}$conversion")
            }
        }
    }

    /**
     * Determines the conversion expression needed to assign [sourceType] to [targetType]
     */
    private fun getConversionExpression(
        sourceType: KSType,
        targetType: KSType,
        defaultValue: String?,
    ): String {
        if (sourceType == targetType) return ""

        // 1. Try Primitive Conversion
        var conversion =
            invokePrimitiveDeclaration(sourceType = sourceType, targetType = targetType)

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
                return if (defaultValue.isNullOrEmpty()) {
                    // No default value, use non-null assertion (potentially unsafe)
                    "!!$conversion"
                } else {
                    // A default value is provided, use Elvis operator
                    "$conversion ?: ${formatDefaultValue(targetType, defaultValue)}"
                }
            } else {
                if (conversion.isNotEmpty()) {
                    return "?$conversion"
                }
            }
        }

        return conversion
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
        defaultValue: String?,
    ): String {
        val sourceDeclaration = sourceType.declaration as? KSClassDeclaration ?: return ""
        val targetDeclaration = targetType.declaration as? KSClassDeclaration ?: return ""

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
                    var transformation = ".map { value -> value$innerConversion }"
                    if (isTargetSet) {
                        transformation += ".toSet()"
                    }
                    return transformation
                }
            }
        }

        return ""
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
    private fun getObjectConversion(sourceType: KSType, targetType: KSType): String {
        val sourceDeclaration = sourceType.declaration
        val targetDeclaration = targetType.declaration

        if (sourceDeclaration is KSClassDeclaration && targetDeclaration is KSClassDeclaration) {
            val sourcePkg = sourceDeclaration.packageName.asString()
            val targetPkg = targetDeclaration.packageName.asString()

            if (!sourcePkg.startsWith(prefix = "kotlin") && !targetPkg.startsWith(prefix = "kotlin")) {
                return ".as${targetDeclaration.simpleName.asString()}()"
            }
        }

        return ""
    }

    /**
     * Generates conversion calls for standard primitive types and Strings
     */
    private fun invokePrimitiveDeclaration(
        sourceType: KSType,
        targetType: KSType,
    ): String {
        val sourceTypeName = sourceType.declaration.simpleName.asString()
        val targetTypeName = targetType.declaration.simpleName.asString()

        return when (sourceTypeName) {
            "String" if targetTypeName == "Long" -> ".toLong()"
            "String" if targetTypeName == "Int" -> ".toInt()"
            "String" if targetTypeName == "Double" -> ".toDouble()"
            "String" if targetTypeName == "Float" -> ".toFloat()"
            "String" if targetTypeName == "Boolean" -> ".toBoolean()"
            "Int" if targetTypeName == "Long" -> ".toLong()"
            "Long" if targetTypeName == "Int" -> ".toInt()"
            "Double" if targetTypeName == "Float" -> ".toFloat()"
            "Float" if targetTypeName == "Double" -> ".toDouble()"
            "Long" if targetTypeName == "String" -> ".toString()"
            "Int" if targetTypeName == "String" -> ".toString()"
            "Double" if targetTypeName == "String" -> ".toString()"
            "Boolean" if targetTypeName == "String" -> ".toString()"
            else -> ""
        }
    }

    /**
     * Formats the default value string based on the target type
     */
    private fun formatDefaultValue(targetType: KSType, defaultValue: String): String {
        val isString = targetType.declaration.simpleName.asString() == "String"

        return if (isString && !defaultValue.startsWith(prefix = "\"")) {
            "\"$defaultValue\""
        } else {
            defaultValue
        }
    }

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
