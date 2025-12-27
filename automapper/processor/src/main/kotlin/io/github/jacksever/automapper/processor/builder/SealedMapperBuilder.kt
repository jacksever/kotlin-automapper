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
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toClassName
import io.github.jacksever.automapper.processor.helper.ParameterHelper.buildConstructorParameters

/**
 * Strategy for generating mapping code for Sealed classes and interfaces
 *
 * This builder ensures that both the source and target sealed hierarchies have a matching structure.
 * It recursively finds all "leaf" subclasses (objects or concrete classes) and verifies that
 * each source leaf has a corresponding target leaf with the same simple name
 *
 * If the hierarchies do not match, the build is failed with a clear error, preventing the generation
 * of a non-exhaustive `when` expression and ensuring compile-time safety
 */
internal class SealedMapperBuilder(private val logger: KSPLogger) : MapperBuilder {

    /**
     * Validates the sealed hierarchies and generates an exhaustive `when` expression if they match
     *
     * Example output:
     * ```
     * return when(this) {
     *     Source.Sub1 -> Target.Sub1
     *     is Source.Sub2 -> Target.Sub2(
     *         prop = prop
     *     )
     * }
     * ```
     */
    override fun buildConversion(from: KSClassDeclaration, to: KSClassDeclaration): CodeBlock =
        buildCodeBlock {
            val targetLeaves = collectLeafSubclasses(declaration = to)
            val sourceLeaves = collectLeafSubclasses(declaration = from)

            val targetLeafNames = targetLeaves.map { entry -> entry.simpleName.asString() }.toSet()
            val sourceLeafNames = sourceLeaves.map { entry -> entry.simpleName.asString() }.toSet()

            val missingInTarget = sourceLeafNames - targetLeafNames
            if (missingInTarget.isNotEmpty()) {
                logger.error(
                    message = buildString {
                        append("Cannot generate sealed mapper from ${from.simpleName.asString()} to ${to.simpleName.asString()}. ")
                        append("Target hierarchy is missing implementations for: ${missingInTarget.joinToString()}. ")
                        append("Please ensure all leaf subclasses have a matching counterpart by name")
                    },
                    symbol = to
                )
                error(message = "Sealed mapping failed due to mismatched hierarchies")
            }

            beginControlFlow(controlFlow = "return when (this)")
            sourceLeaves.forEach { source ->
                val targetSub =
                    targetLeaves.first { target -> target.simpleName == source.simpleName }

                if (source.classKind == ClassKind.OBJECT) {
                    addStatement("%T -> %T", source.toClassName(), targetSub.toClassName())
                } else {
                    val params =
                        buildConstructorParameters(sourceClass = source, targetClass = targetSub)

                    add("is %T -> %T(\n", source.toClassName(), targetSub.toClassName())
                    indent()
                    params.forEach { param -> addStatement("$param,") }
                    unindent()
                    add(")\n")
                }
            }
            endControlFlow()
        }

    /**
     * Recursively collects all leaf subclasses (concrete classes or objects) of a sealed class/interface
     */
    private fun collectLeafSubclasses(declaration: KSClassDeclaration): List<KSClassDeclaration> {
        if (!declaration.modifiers.contains(element = Modifier.SEALED)) {
            return listOf(declaration)
        }

        return declaration.getSealedSubclasses()
            .toList()
            .flatMap(transform = ::collectLeafSubclasses)
    }
}
