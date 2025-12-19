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
 * Strategy for generating mapping code for Sealed classes (and interfaces)
 *
 * This builder handles class hierarchies by generating a exhaustive `when(this)` expression.
 * It recursively finds all "leaf" subclasses (objects or concrete classes) of the source sealed class
 * and attempts to find a matching subclass in the target sealed class hierarchy by simple name
 */
internal class SealedMapperBuilder(private val logger: KSPLogger) : MapperBuilder {

    /**
     * Generates a `when` expression to map each subclass of the source sealed hierarchy
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
            beginControlFlow("return when(this)")

            val leaves = collectLeafSubclasses(declaration = from)
            logger.info("SealedMapperBuilder: Found ${leaves.size} leaf subclasses for sealed class ${from.simpleName.asString()}")

            leaves.forEach { subSource ->
                val subTarget = findMatchingSubclass(sourceSub = subSource, targetRoot = to)

                if (subTarget != null) {
                    if (subSource.classKind == ClassKind.OBJECT) {
                        addStatement("%T -> %T", subSource.toClassName(), subTarget.toClassName())
                    } else {
                        val params = buildConstructorParameters(
                            sourceClass = subSource,
                            targetClass = subTarget,
                        )

                        add("is %T -> %T(\n", subSource.toClassName(), subTarget.toClassName())
                        indent()
                        params.forEach { param -> addStatement("$param,") }
                        unindent()
                        add(")\n")
                    }
                } else {
                    logger.warn("SealedMapperBuilder: No matching subclass found in ${to.simpleName.asString()} for ${subSource.simpleName.asString()}")
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

        var subclasses = declaration.getSealedSubclasses().toList()

        if (subclasses.isEmpty()) {
            subclasses = declaration.declarations
                .filterIsInstance<KSClassDeclaration>()
                .filter { nested ->
                    nested.superTypes.any { type ->
                        type.resolve().declaration.qualifiedName == declaration.qualifiedName
                    }
                }
                .toList()
        }

        return subclasses.flatMap(::collectLeafSubclasses)
    }

    /**
     * Finds a subclass in the [targetRoot] hierarchy that matches the [sourceSub] by simple name
     */
    private fun findMatchingSubclass(
        sourceSub: KSClassDeclaration,
        targetRoot: KSClassDeclaration
    ): KSClassDeclaration? {
        val sourceName = sourceSub.simpleName.asString()

        return findAllNestedClasses(declaration = targetRoot)
            .firstOrNull { nestedClass -> nestedClass.simpleName.asString() == sourceName }
    }

    /**
     * Recursively collects all nested classes within a class declaration
     */
    private fun findAllNestedClasses(declaration: KSClassDeclaration): Sequence<KSClassDeclaration> {
        val nested = declaration.declarations.filterIsInstance<KSClassDeclaration>()

        return nested + nested.flatMap(::findAllNestedClasses)
    }
}
