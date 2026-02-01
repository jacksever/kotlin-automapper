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
        getInnerConversion: (
            sourceType: KSType,
            targetType: KSType,
            isShadowed: Boolean,
            defaultValue: DefaultValue?
        ) -> CodeBlock,
    ): CodeBlock {
        val sourceDeclaration = sourceType.declaration as? KSClassDeclaration ?: return EMPTY
        val targetDeclaration = targetType.declaration as? KSClassDeclaration ?: return EMPTY

        val sourceKind = sourceDeclaration.collectionKind() ?: return EMPTY
        val targetKind = targetDeclaration.collectionKind() ?: return EMPTY

        val sourceArg = sourceType.arguments.firstOrNull()?.type?.resolve() ?: return EMPTY
        val targetArg = targetType.arguments.firstOrNull()?.type?.resolve() ?: return EMPTY

        val innerConversion =
            getInnerConversion(sourceArg, targetArg, isShadowed, defaultValue)

        val needsElementConversion = innerConversion.isNotEmpty()
        val needsContainerConversion = sourceKind != targetKind

        if (!needsElementConversion && !needsContainerConversion) {
            return EMPTY
        }

        return buildCodeBlock {
            add(".map { value -> value%L }", innerConversion)

            if (targetKind == CollectionKind.SET) {
                add(".toSet()")
            }
        }
    }

    /**
     * An empty conversion expression
     *
     * Used as a default or fallback when no specific conversion is needed
     */
    private val EMPTY = CodeBlock.of("")

    /**
     * Represents the kind of a collection, distinguishing between List and Set types
     */
    private enum class CollectionKind {

        /**
         * A boolean flag indicating if the collection kind is a `List`
         */
        LIST,

        /**
         * A boolean flag indicating if the collection kind is a `Set`
         */
        SET,
        ;
    }

    /**
     * Determines the [CollectionKind] (List or Set) of a given class declaration
     */
    private fun KSClassDeclaration.collectionKind(): CollectionKind? =
        when (qualifiedName?.asString()) {
            "kotlin.collections.List",
            "kotlin.collections.MutableList",
            "java.util.List" -> CollectionKind.LIST

            "kotlin.collections.Set",
            "kotlin.collections.MutableSet",
            "java.util.Set" -> CollectionKind.SET

            else -> null
        }
}
