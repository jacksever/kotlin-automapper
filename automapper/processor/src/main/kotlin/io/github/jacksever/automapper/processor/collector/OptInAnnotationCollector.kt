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

package io.github.jacksever.automapper.processor.collector

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import io.github.jacksever.automapper.processor.model.MappingContext
import io.github.jacksever.automapper.processor.model.MappingType

/**
 * A common interface for collecting OptIn annotations
 */
internal interface OptInAnnotationCollector {

    /**
     * Collects all required opt-in markers and builds a single [AnnotationSpec] for the `@OptIn` annotation
     *
     * @param source resolved source type
     * @param target resolved target type
     * @param context converters recorded during expression generation
     * @return [AnnotationSpec] for the `@OptIn` annotation or null if no opt-in markers are found
     */
    fun collectOptInAnnotations(
        source: MappingType,
        target: MappingType,
        context: MappingContext,
    ): AnnotationSpec?
}

/**
 * This class encapsulates the logic for finding all necessary OptIn annotations for a given mapping.
 * It recursively scans sealed hierarchies and combines all found markers into a single, valid
 * `@OptIn` annotation
 */
internal class OptInAnnotationCollectorImpl(
    private val logger: KSPLogger,
) : OptInAnnotationCollector {

    override fun collectOptInAnnotations(
        source: MappingType,
        target: MappingType,
        context: MappingContext,
    ): AnnotationSpec? {
        val declarations = (listOf(source, target) + context.types.leaves(root = source) +
                context.types.leaves(root = target))
            .map { type -> type.declaration }
            .distinct()

        val annotatedSymbols = sequence {
            declarations.forEach { declaration ->
                yield(value = declaration)
                yieldAll(sequence = declaration.getAllProperties())
            }

            context.usedConverters.forEach { converter ->
                yield(value = converter.function)
                yield(value = converter.owner)
                (converter.function.parentDeclaration as? KSClassDeclaration)?.let { owner ->
                    yield(value = owner)
                }
            }
        }

        val classNames = annotatedSymbols
            .flatMap { symbol -> symbol.optInMarkers() }
            .map { marker -> marker.toClassName() }
            .distinct()
            .sortedBy { className -> className.canonicalName }
            .toList()

        if (classNames.isEmpty()) {
            logger.info(message = "OptInAnnotationCollector: No markers found. Skipping annotation")
            return null
        }

        val format = classNames.joinToString { "%T::class" }

        return AnnotationSpec.builder(type = ClassName("kotlin", "OptIn"))
            .addMember(format, *classNames.toTypedArray())
            .build()
    }

    /** Extracts the marker types explicitly listed by this symbol's `@OptIn` annotations */
    private fun KSAnnotated.optInMarkers(): Sequence<KSType> = annotations
        .filter { annotation ->
            annotation.annotationType.resolve().declaration.qualifiedName?.asString() == "kotlin.OptIn"
        }
        .flatMap { annotation ->
            (annotation.arguments.firstOrNull()?.value as? List<*>)
                .orEmpty()
                .filterIsInstance<KSType>()
        }
}
