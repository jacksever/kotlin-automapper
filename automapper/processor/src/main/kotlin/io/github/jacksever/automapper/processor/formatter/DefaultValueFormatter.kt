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

package io.github.jacksever.automapper.processor.formatter

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ksp.toClassName
import io.github.jacksever.automapper.processor.extension.isEnum
import io.github.jacksever.automapper.processor.extension.isSealed

/**
 * Helper object for formatting default value strings into typed [CodeBlock]s
 *
 * This object inspects the target type and formats the provided string value accordingly
 * e.g. adding quotes for strings, or resolving enum/sealed class members
 */
internal object DefaultValueFormatter {

    /**
     * Formats the default value string into a [CodeBlock] based on the target type
     */
    fun format(targetType: KSType, defaultValue: String): CodeBlock =
        when {
            targetType.isKotlinString() -> {
                CodeBlock.of("%S", defaultValue.removeSurrounding(delimiter = "\""))
            }

            targetType.isKotlinChar() -> {
                val charValue = defaultValue.trim('\'')
                require(charValue.length == 1) { "Default value '$defaultValue' is not a valid Char literal" }

                CodeBlock.of("'%L'", charValue)
            }

            targetType.isEnum() || targetType.isSealed() -> {
                val entry = defaultValue.substringAfterLast(delimiter = '.')
                val className = (targetType.declaration as KSClassDeclaration).toClassName()

                CodeBlock.of("%T.%L", className, entry)
            }

            else -> CodeBlock.of("%L", defaultValue)
        }

    /**
     * Checks if the [KSType] represents the built-in `kotlin.String` type
     */
    private fun KSType.isKotlinString(): Boolean =
        declaration.qualifiedName?.asString() == "kotlin.String"

    /**
     * Checks if the [KSType] represents the primitive `kotlin.Char` type
     */
    private fun KSType.isKotlinChar(): Boolean =
        declaration.qualifiedName?.asString() == "kotlin.Char"
}
