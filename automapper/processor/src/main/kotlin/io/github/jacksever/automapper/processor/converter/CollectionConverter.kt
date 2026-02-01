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

package io.github.jacksever.automapper.processor.converter

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import io.github.jacksever.automapper.annotation.DefaultValue

/**
 * A helper object that provides conversion expressions for mapping between different collection types
 *
 * This object checks if both types are supported collections and recursively generates conversions
 * for their type arguments. It also handles `List <-> Set` transformations
 */
internal object CollectionConverter {

    /**
     * Attempts to generate a conversion expression for collection types (List, Set)
     */
    fun getConversion(
        sourceType: KSType,
        targetType: KSType,
        isShadowed: Boolean,
        defaultValue: DefaultValue?,
        getInnerConversion: (sourceType: KSType, targetType: KSType, isShadowed: Boolean, defaultValue: DefaultValue?) -> CodeBlock,
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
                val innerConversion =
                    getInnerConversion(sourceArg, targetArg, isShadowed, defaultValue)

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
