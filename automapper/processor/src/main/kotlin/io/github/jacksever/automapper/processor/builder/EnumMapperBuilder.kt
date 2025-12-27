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

package io.github.jacksever.automapper.processor.builder

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * Strategy for generating mapping code for Enum classes
 *
 * This builder generates an exhaustive `when` expression. If a source enum constant has a matching
 * counterpart in the target enum, it's mapped directly. If not, a runtime
 * [IllegalArgumentException] is thrown for that specific constant, ensuring that the
 * mapping is safe at compile time and provides clear feedback at runtime for unmapped values
 */
internal class EnumMapperBuilder(private val logger: KSPLogger) : MapperBuilder {

    /**
     * Generates an exhaustive `when` expression for the mapping
     *
     * Example output for a missing constant:
     * ```
     * return when (this) {
     *     SourceEnum.ACTIVE -> TargetEnum.ACTIVE
     *     SourceEnum.DELETED -> throw IllegalArgumentException("Enum constant ... has no matching constant ...")
     * }
     * ```
     */
    override fun buildConversion(from: KSClassDeclaration, to: KSClassDeclaration): CodeBlock =
        buildCodeBlock {
            val fromEntries = from.declarations
                .filterIsInstance<KSClassDeclaration>()
                .filter { declaration -> declaration.classKind == ClassKind.ENUM_ENTRY }
                .map { entry -> entry.simpleName.asString() }
                .toSet()

            val toEntries = to.declarations
                .filterIsInstance<KSClassDeclaration>()
                .filter { declaration -> declaration.classKind == ClassKind.ENUM_ENTRY }
                .map { entry -> entry.simpleName.asString() }
                .toSet()

            beginControlFlow(controlFlow = "return when (this)")
            fromEntries.forEach { entryName ->
                if (entryName in toEntries) {
                    addStatement(
                        "%T.%L -> %T.%L",
                        from.toClassName(),
                        entryName,
                        to.toClassName(),
                        entryName
                    )
                } else {
                    val errorMessage =
                        "Enum constant ${from.simpleName.asString()}.$entryName has no matching constant in ${to.simpleName.asString()}"
                    logger.warn(message = errorMessage, symbol = from)

                    addStatement(
                        "%T.%L -> throw %T(%S)",
                        from.toClassName(),
                        entryName,
                        IllegalArgumentException::class,
                        errorMessage
                    )
                }
            }
            endControlFlow()
        }
}
