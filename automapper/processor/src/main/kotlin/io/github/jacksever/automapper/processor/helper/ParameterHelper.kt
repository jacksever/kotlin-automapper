/*
 * Copyright (c) 2025 Alexander Gorodnikov
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

/**
 * Helper object for generating constructor parameters and handling type conversions.
 *
 * This object provides utility methods to match properties between source and target classes
 * and generate the appropriate assignment code, including type conversions if necessary.
 */
internal object ParameterHelper {

    /**
     * Builds a list of constructor parameter assignments for the target class
     * based on properties from the source class, applying custom mappings.
     *
     * @param sourceClass Source class declaration.
     * @param targetClass Target class declaration.
     * @param customMappings A map of custom mappings from a target property name to a source property name.
     * @return List of strings representing constructor arguments (e.g., "id = sourceId").
     */
    fun buildConstructorParameters(
        sourceClass: KSClassDeclaration,
        targetClass: KSClassDeclaration,
        customMappings: Map<String, String> = emptyMap(),
    ): List<String> = buildList {
        val sourceProperties = sourceClass.getAllProperties().associateBy { it.simpleName.asString() }
        val targetConstructorParams = targetClass.primaryConstructor?.parameters ?: emptyList()

        targetConstructorParams.forEach { targetParam ->
            val targetParamName = targetParam.name!!.asString()

            // Find the source property name: use the custom mapping, or fall back to the same name.
            val sourcePropName = customMappings[targetParamName] ?: targetParamName

            sourceProperties[sourcePropName]?.let { sourceProperty ->
                val targetType = targetParam.type.resolve()
                val sourceType = sourceProperty.type.resolve()
                val conversion = getConversionExpression(sourceType = sourceType, targetType = targetType)

                // Assign from the source property, applying any necessary conversion.
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
    ): String {
        if (sourceType == targetType) return ""

        // 1. Try Primitive Conversion
        var conversion =
            invokePrimitiveDeclaration(sourceType = sourceType, targetType = targetType)

        // 2. Try Collection Conversion
        if (conversion.isEmpty()) {
            conversion = getCollectionConversion(sourceType = sourceType, targetType = targetType)
        }

        // 3. Try Object Conversion
        if (conversion.isEmpty()) {
            conversion = getObjectConversion(sourceType = sourceType, targetType = targetType)
        }

        // 4. Handle Nullability
        if (sourceType.isMarkedNullable) {
            if (!targetType.isMarkedNullable) {
                // Source? -> Target (Non-null). We need to unwrap: "!!"
                return "!!$conversion"
            } else {
                // Source? -> Target?. We need safe call if conversion is present: "?"
                if (conversion.isNotEmpty()) {
                    return "?$conversion"
                }
            }
        }

        return conversion
    }

    /**
     * Attempts to generate a conversion expression for collection types (List, Set)
     */
    private fun getCollectionConversion(sourceType: KSType, targetType: KSType): String {
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
                val innerConversion =
                    getConversionExpression(sourceType = sourceArg, targetType = targetArg)

                // If elements need conversion OR container type changes (e.g. Set -> List)
                if (innerConversion.isNotEmpty() || (isSourceSet && isTargetList) || (isSourceList && isTargetSet)) {
                    var transformation = ".map { it$innerConversion }"
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

        return when {
            sourceTypeName == "String" && targetTypeName == "Long" -> ".toLong()"
            sourceTypeName == "String" && targetTypeName == "Int" -> ".toInt()"
            sourceTypeName == "String" && targetTypeName == "Double" -> ".toDouble()"
            sourceTypeName == "String" && targetTypeName == "Float" -> ".toFloat()"
            sourceTypeName == "String" && targetTypeName == "Boolean" -> ".toBoolean()"

            sourceTypeName == "Int" && targetTypeName == "Long" -> ".toLong()"
            sourceTypeName == "Long" && targetTypeName == "Int" -> ".toInt()"
            sourceTypeName == "Double" && targetTypeName == "Float" -> ".toFloat()"
            sourceTypeName == "Float" && targetTypeName == "Double" -> ".toDouble()"

            sourceTypeName == "Long" && targetTypeName == "String" -> ".toString()"
            sourceTypeName == "Int" && targetTypeName == "String" -> ".toString()"
            sourceTypeName == "Double" && targetTypeName == "String" -> ".toString()"
            sourceTypeName == "Boolean" && targetTypeName == "String" -> ".toString()"

            else -> ""
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
