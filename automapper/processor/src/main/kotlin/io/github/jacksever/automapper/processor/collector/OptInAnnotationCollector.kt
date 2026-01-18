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
 * A dedicated collector for OptIn annotations
 *
 * This class encapsulates the logic for finding all necessary OptIn annotations for a given mapping.
 * It recursively scans sealed hierarchies and combines all found markers into a single, valid
 * `@OptIn` annotation
 */
internal class OptInAnnotationCollector {

    /**
     * Collects all required opt-in markers and builds a single [com.squareup.kotlinpoet.AnnotationSpec] for the `@OptIn` annotation
     */
    fun collect(
        definition: MapperDefinition,
        source: KSClassDeclaration,
        target: KSClassDeclaration,
    ): AnnotationSpec? {
        val allAnnotations = mutableSetOf<KSAnnotation>()

        // Find all reachable declarations from source and target through sealed hierarchies
        val sourceDeclarations = getAllDeclarations(declaration = source)
        val targetDeclarations = getAllDeclarations(declaration = target)

        // Collect annotations from all found declarations and their properties
        (sourceDeclarations + targetDeclarations).forEach { declaration ->
            allAnnotations.addAll(elements = declaration.annotations)
            declaration.getAllProperties().forEach { property ->
                allAnnotations.addAll(elements = property.annotations)
            }
        }

        // For each pair of corresponding data classes in the hierarchies, check for converters
        val sourceDataClasses =
            sourceDeclarations.filter { declaration -> declaration.modifiers.contains(Modifier.DATA) }
        val targetDataClasses =
            targetDeclarations.filter { declaration -> declaration.modifiers.contains(Modifier.DATA) }

        sourceDataClasses.forEach { sourceData ->
            targetDataClasses
                .find { declaration -> declaration.simpleName.asString() == sourceData.simpleName.asString() }
                ?.let { targetData ->
                    val sourceProperties = sourceData.getAllProperties()
                        .associateBy { property -> property.simpleName.asString() }
                    val targetConstructorParams =
                        targetData.primaryConstructor?.parameters ?: emptyList()

                    for (targetParam in targetConstructorParams) {
                        val targetName = targetParam.name?.asString() ?: continue
                        val sourceName = definition.propertyMappings
                            .find { mapping -> mapping.to == targetName }?.from
                            ?: targetName

                        sourceProperties[sourceName]?.let { sourceProperty ->
                            val sourceType = sourceProperty.type.resolve()
                            val targetType = targetParam.type.resolve()

                            if (sourceType.makeNotNullable() != targetType.makeNotNullable()) {
                                definition.converters
                                    .find { definition ->
                                        definition.from.makeNotNullable() == sourceType.makeNotNullable() && definition.to.makeNotNullable() == targetType.makeNotNullable()
                                    }
                                    ?.let { converter ->
                                        allAnnotations.addAll(elements = converter.function.annotations)
                                        (converter.function.parent as? KSClassDeclaration)?.let { declaration ->
                                            allAnnotations.addAll(elements = declaration.annotations)
                                        }
                                    }
                            }
                        }
                    }
                }
        }

        // Extract all marker classes from the collected OptIn annotations
        val optInMarkerClasses = allAnnotations
            .filter { annotation ->
                val annotationType = annotation.annotationType.resolve()

                annotation.shortName.asString() == "OptIn" && annotationType.declaration.qualifiedName?.asString() == "kotlin.OptIn"
            }
            .flatMap { optInAnnotation ->
                (optInAnnotation.arguments.first().value as? List<*>)
                    ?.filterIsInstance<KSType>()
                    .orEmpty()
            }
            .map { type -> type.toClassName() }
            .distinct()
            .sortedBy { className -> className.canonicalName } // Sort for consistent output

        if (optInMarkerClasses.isEmpty()) {
            return null
        }

        // Build a single AnnotationSpec for @OptIn with all the collected markers
        val format = optInMarkerClasses.joinToString(separator = ", ") { "%T::class" }

        return AnnotationSpec.builder(type = ClassName("kotlin", "OptIn"))
            .addMember(format, *optInMarkerClasses.toTypedArray())
            .build()
    }

    /**
     * Recursively finds all nested declarations within a sealed hierarchy
     */
    private fun getAllDeclarations(declaration: KSClassDeclaration): Set<KSClassDeclaration> {
        val declarations = mutableSetOf<KSClassDeclaration>()

        fun find(decl: KSClassDeclaration) {
            if (declarations.add(element = decl)) {
                if (decl.modifiers.contains(Modifier.SEALED)) {
                    decl.getSealedSubclasses().forEach(action = ::find)
                }
            }
        }

        find(decl = declaration)

        return declarations
    }
}