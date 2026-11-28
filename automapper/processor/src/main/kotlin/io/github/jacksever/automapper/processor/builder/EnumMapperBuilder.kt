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

package io.github.jacksever.automapper.processor.builder

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toClassName
import io.github.jacksever.automapper.annotation.PropertyMapping
import io.github.jacksever.automapper.processor.model.MappingType

/**
 * Strategy for generating mapping code for Enum classes
 *
 * This builder generates an exhaustive `when` expression. If a source enum constant has a matching
 * counterpart in the target enum, it's mapped directly. If not, a runtime
 * [IllegalArgumentException] is thrown for that specific constant, ensuring that the
 * mapping is safe at compile time and provides clear feedback at runtime for unmapped values
 */
internal class EnumMapperBuilder(
    private val logger: KSPLogger,
    private val propertyMappings: List<PropertyMapping>,
) : MapperBuilder {

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
    override fun buildConversion(from: MappingType, to: MappingType): CodeBlock =
        buildCodeBlock {
            val fromEntries = from.declaration.enumEntryNames()
            val toEntries = to.declaration.enumEntryNames()

            val customMappings =
                propertyMappings.associate { property -> property.from to property.to }

            beginControlFlow(controlFlow = "return when (this)")
            fromEntries.forEach { sourceEntryName ->
                val targetEntryName = customMappings[sourceEntryName] ?: sourceEntryName

                if (targetEntryName in toEntries) {
                    addStatement(
                        "%T.%N -> %T.%N",
                        from.declaration.toClassName(),
                        sourceEntryName,
                        to.declaration.toClassName(),
                        targetEntryName
                    )
                } else {
                    val errorMessage =
                        "Enum constant ${from.declaration.simpleName.asString()}.$sourceEntryName has no matching constant in ${to.declaration.simpleName.asString()}"
                    logger.warn(message = errorMessage, symbol = from.declaration)

                    addStatement(
                        "%T.%N -> throw %T(%S)",
                        from.declaration.toClassName(),
                        sourceEntryName,
                        ClassName("kotlin", "IllegalArgumentException"),
                        errorMessage
                    )
                }
            }
            endControlFlow()
        }

    /**
     * Retrieves the names of all enum entries declared within this class declaration
     */
    private fun KSClassDeclaration.enumEntryNames(): Set<String> = declarations
        .filterIsInstance<KSClassDeclaration>()
        .filter { declaration -> declaration.classKind == ClassKind.ENUM_ENTRY }
        .map { entry -> entry.simpleName.asString() }
        .toSet()
}
