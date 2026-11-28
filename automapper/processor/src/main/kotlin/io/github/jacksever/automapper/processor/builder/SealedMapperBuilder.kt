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
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toTypeName
import io.github.jacksever.automapper.annotation.DefaultValue
import io.github.jacksever.automapper.annotation.PropertyMapping
import io.github.jacksever.automapper.processor.helper.ParameterHelper.buildConstructorParameters
import io.github.jacksever.automapper.processor.model.ConverterDefinition
import io.github.jacksever.automapper.processor.model.MappingContext
import io.github.jacksever.automapper.processor.model.MappingType

/**
 * Strategy for generating mapping code for Sealed classes and interfaces
 *
 * This builder ensures that both the source and target sealed hierarchies have a matching structure.
 * It recursively finds all "leaf" subclasses and verifies that each source leaf has a
 * corresponding target leaf, using custom [PropertyMapping] rules if provided
 *
 * If the hierarchies do not match, the build is failed with a clear error, preventing the generation
 * of a non-exhaustive `when` expression and ensuring compile-time safety
 */
internal class SealedMapperBuilder(
    private val logger: KSPLogger,
    private val context: MappingContext,
    private val defaultValues: List<DefaultValue>,
    private val converters: List<ConverterDefinition>,
    private val propertyMappings: List<PropertyMapping>,
) : MapperBuilder {

    /**
     * Validates the sealed hierarchies and generates an exhaustive `when` expression if they match
     *
     * A fallback branch covers nominal subclasses excluded by the receiver's generic arguments,
     * because Kotlin can still require those unreachable cases for exhaustiveness
     *
     * Example output:
     * ```
     * return when(this) {
     *     Source.Sub1 -> Target.Sub1
     *     is Source.Sub2 -> Target.LegacySub2(
     *         prop = prop
     *     )
     * }
     * ```
     */
    override fun buildConversion(from: MappingType, to: MappingType): CodeBlock =
        buildCodeBlock {
            val targetLeaves = context.types.leaves(root = to)
            val sourceLeaves = context.types.leaves(root = from)

            val sourceLeafNames = sourceLeaves
                .map { leaf -> leaf.declaration.simpleName.asString() }.toSet()
            val (classMappings, fieldMappings) = propertyMappings
                .partition { property -> property.from in sourceLeafNames }
            val customClassMappings = classMappings
                .associate { property -> property.from to property.to }

            val targetLeafNames =
                targetLeaves.map { entry -> entry.declaration.simpleName.asString() }.toSet()
            val missingInTarget = sourceLeaves.mapNotNull { sourceLeaf ->
                val sourceName = sourceLeaf.declaration.simpleName.asString()
                val targetName = customClassMappings[sourceName] ?: sourceName

                if (targetName !in targetLeafNames) sourceName else null
            }

            if (missingInTarget.isNotEmpty()) {
                logger.error(
                    message = buildString {
                        append("Cannot generate sealed mapper from ${from.declaration.simpleName.asString()} to ${to.declaration.simpleName.asString()}. ")
                        append("Target hierarchy is missing implementations for: ${missingInTarget.joinToString()}. ")
                        append("Please ensure all leaf subclasses have a matching counterpart by name, or provide a @PropertyMapping for them")
                    },
                    symbol = to.declaration
                )
                error(message = "Sealed mapping failed due to mismatched hierarchies")
            }

            beginControlFlow(controlFlow = "return when (this)")
            sourceLeaves.forEach { source ->
                val sourceName = source.declaration.simpleName.asString()
                val targetName = customClassMappings[sourceName] ?: sourceName
                val targetSub = targetLeaves
                    .first { target -> target.declaration.simpleName.asString() == targetName }

                if (source.declaration.classKind == ClassKind.OBJECT) {
                    addStatement(
                        "%T -> %T",
                        source.type.toTypeName(typeParamResolver = context.typeParameters),
                        targetSub.constructorType.toTypeName(typeParamResolver = context.typeParameters),
                    )
                } else {
                    val params = buildConstructorParameters(
                        source = source,
                        logger = logger,
                        context = context,
                        target = targetSub,
                        converters = converters,
                        defaultValues = defaultValues,
                        propertyMappings = fieldMappings,
                    )

                    add(
                        "is %T -> %T(\n",
                        source.type.toTypeName(typeParamResolver = context.typeParameters),
                        targetSub.constructorType.toTypeName(typeParamResolver = context.typeParameters),
                    )
                    indent()
                    params
                        .filter { parameter -> parameter.isNotEmpty() }
                        .forEach { param ->
                            add(param)
                            add(",\n")
                        }
                    unindent()
                    add(")\n")
                }
            }

            if (sourceLeaves.size < context.types.leafDeclarations(declaration = from.declaration).size) {
                addStatement(
                    "else -> throw %T(%S)",
                    ClassName("kotlin", "IllegalStateException"),
                    "Unexpected sealed subtype for ${from.type}"
                )
            }
            endControlFlow()
        }
}
