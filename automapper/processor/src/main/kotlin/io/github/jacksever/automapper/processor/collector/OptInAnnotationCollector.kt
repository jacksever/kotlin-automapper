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

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import io.github.jacksever.automapper.processor.model.MapperDefinition

/**
 * A common interface for collecting OptIn annotations
 */
internal interface OptInAnnotationCollector {

    /**
     * Collects all required opt-in markers and builds a single [AnnotationSpec] for the `@OptIn` annotation
     *
     * @param definition [MapperDefinition] for the current mapping
     * @param source source class declaration
     * @param target target class declaration
     * @return [AnnotationSpec] for the `@OptIn` annotation or null if no opt-in markers are found
     */
    fun collectOptInAnnotations(
        definition: MapperDefinition,
        source: KSClassDeclaration,
        target: KSClassDeclaration,
    ): AnnotationSpec?
}

/**
 * This class encapsulates the logic for finding all necessary OptIn annotations for a given mapping.
 * It recursively scans sealed hierarchies and combines all found markers into a single, valid
 * `@OptIn` annotation
 */
internal class OptInAnnotationCollectorImpl : OptInAnnotationCollector {

    override fun collectOptInAnnotations(
        definition: MapperDefinition,
        source: KSClassDeclaration,
        target: KSClassDeclaration,
    ): AnnotationSpec? {
        val optInMarkers = mutableSetOf<KSType>()

        // Collect all reachable declarations from sealed hierarchies
        val sourceDeclarations = getAllDeclarations(root = source)
        val targetDeclarations = getAllDeclarations(root = target)

        // Collect OptIn markers from declarations and their properties
        (sourceDeclarations + targetDeclarations).forEach { declaration ->
            collectOptInMarkers(annotations = declaration.annotations, out = optInMarkers)

            declaration.getAllProperties().forEach { property ->
                collectOptInMarkers(annotations = property.annotations, out = optInMarkers)
            }
        }

        // Prepare fast lookup structures
        val sourceDataClasses = sourceDeclarations
            .filter { declaration -> Modifier.DATA in declaration.modifiers }
            .associateBy { declaration -> declaration.simpleName.asString() }

        val targetDataClasses = targetDeclarations
            .filter { declaration -> Modifier.DATA in declaration.modifiers }

        val convertersByType = definition.converters.associateBy { definition ->
            definition.from.makeNotNullable() to definition.to.makeNotNullable()
        }

        // Check converters between corresponding data classes
        targetDataClasses.forEach { targetData ->
            val sourceData = sourceDataClasses[targetData.simpleName.asString()] ?: return@forEach
            val sourceProperties = sourceData.getAllProperties()
                .associateBy { property -> property.simpleName.asString() }
            val targetParams = targetData.primaryConstructor?.parameters.orEmpty()

            targetParams.forEach { targetParam ->
                val targetName = targetParam.name?.asString() ?: return@forEach

                val sourceName = definition.propertyMappings
                    .find { mapping -> mapping.to == targetName }
                    ?.from ?: targetName

                val sourceProperty = sourceProperties[sourceName] ?: return@forEach

                val sourceType = sourceProperty.type.resolve().makeNotNullable()
                val targetType = targetParam.type.resolve().makeNotNullable()

                if (sourceType == targetType) return@forEach

                val converter = convertersByType[sourceType to targetType] ?: return@forEach

                collectOptInMarkers(
                    annotations = converter.function.annotations,
                    out = optInMarkers,
                )

                (converter.function.parent as? KSClassDeclaration)?.let { parent ->
                    collectOptInMarkers(annotations = parent.annotations, out = optInMarkers)
                }
            }
        }

        if (optInMarkers.isEmpty()) return null

        val classNames = optInMarkers
            .map { type -> type.toClassName() }
            .distinct()
            .sortedBy { className -> className.canonicalName }

        val format = classNames.joinToString { "%T::class" }

        return AnnotationSpec.builder(type = ClassName("kotlin", "OptIn"))
            .addMember(format, *classNames.toTypedArray())
            .build()
    }

    /**
     * Extracts OptIn marker types from annotations and adds them to [out]
     */
    private fun collectOptInMarkers(
        annotations: Sequence<KSAnnotation>,
        out: MutableSet<KSType>,
    ) = runCatching {
        annotations.forEach { annotation ->
            check(annotation.shortName.asString() == "OptIn")

            val declaration = annotation.annotationType.resolve().declaration
            check(declaration.qualifiedName?.asString() == "kotlin.OptIn")

            val markers = annotation.arguments.firstOrNull()?.value as? List<*>

            markers
                ?.filterIsInstance<KSType>()
                ?.forEach(action = out::add)
        }
    }

    private fun getAllDeclarations(root: KSClassDeclaration): Set<KSClassDeclaration> {
        val result = mutableSetOf<KSClassDeclaration>()
        val stack = ArrayDeque<KSClassDeclaration>()

        stack += root

        while (stack.isNotEmpty()) {
            val current = stack.removeFirst()
            if (!result.add(current)) continue

            if (Modifier.SEALED in current.modifiers) {
                current.getSealedSubclasses().forEach(action = stack::add)
            }
        }

        return result
    }
}
