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

package io.github.jacksever.automapper.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import io.github.jacksever.automapper.annotation.AutoMapper
import io.github.jacksever.automapper.annotation.AutoMapperModule
import io.github.jacksever.automapper.annotation.PropertyMapping
import io.github.jacksever.automapper.processor.builder.MapperBuilderFactory
import io.github.jacksever.automapper.processor.collector.ConverterCollector
import io.github.jacksever.automapper.processor.collector.ConverterCollectorImpl
import io.github.jacksever.automapper.processor.collector.MapperDefinitionCollector
import io.github.jacksever.automapper.processor.collector.MapperDefinitionCollectorImpl
import io.github.jacksever.automapper.processor.collector.OptInAnnotationCollector
import io.github.jacksever.automapper.processor.collector.OptInAnnotationCollectorImpl
import io.github.jacksever.automapper.processor.extenstion.toJoinedSimpleName
import io.github.jacksever.automapper.processor.helper.RuntimeParameterHelper
import io.github.jacksever.automapper.processor.helper.RuntimeParameterHelperImpl
import io.github.jacksever.automapper.processor.model.MapperDefinition

/**
 * Main KSP Processor for generating type-safe mapper extensions
 *
 * This processor scans for interfaces annotated with `@AutoMapperModule`, finds functions
 * annotated with `@AutoMapper`, and generates Kotlin extension functions to convert
 * between source and target types
 */
internal class AutoMapperProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor,
    ConverterCollector by ConverterCollectorImpl(logger = logger),
    RuntimeParameterHelper by RuntimeParameterHelperImpl(logger = logger),
    OptInAnnotationCollector by OptInAnnotationCollectorImpl(logger = logger),
    MapperDefinitionCollector by MapperDefinitionCollectorImpl(logger = logger) {

    override fun process(resolver: Resolver): List<KSAnnotated> = runCatching {
        logger.info(message = "AutoMapperProcessor: Starting processing round...")

        val symbols =
            resolver.getSymbolsWithAnnotation(checkNotNull(AutoMapperModule::class.qualifiedName))
        val (validSymbols, invalidSymbols) = symbols.partition { symbol -> symbol.validate() }

        logger.info(message = "AutoMapperProcessor: Found ${symbols.toList().size} annotated symbols. Valid: ${validSymbols.size}, Invalid (deferred): ${invalidSymbols.size}")

        validSymbols
            .filterIsInstance<KSClassDeclaration>()
            .forEach { module ->
                logger.info(message = "AutoMapperProcessor: Processing module '${module.simpleName.asString()}'")

                val globalConverters = module.annotations
                    .first { annotation ->
                        annotation.shortName.asString() == AutoMapperModule::class.simpleName
                    }
                    .let { annotation ->
                        collectConverters(annotation = annotation)
                    }

                val mappers = module.declarations
                    .filterIsInstance<KSFunctionDeclaration>()
                    .mapNotNull { function ->
                        function.annotations
                            .firstOrNull { annotation ->
                                annotation.shortName.asString() == AutoMapper::class.simpleName
                            }
                            ?.let { annotation ->
                                val localConverters = collectConverters(annotation = annotation)
                                val allConverters = (localConverters + globalConverters)
                                    .groupBy { converter -> "${converter.from}" to "${converter.to}" }
                                    .mapValues { (types, group) ->
                                        if (group.size > 1) {
                                            logger.warn(
                                                message = "Found multiple converters for ${types.first} -> ${types.second}. Prioritizing the one from @AutoMapper",
                                                symbol = function
                                            )
                                        }

                                        // Local converters are first in the list, so they take priority
                                        group.first()
                                    }
                                    .values
                                    .toList()

                                collectMapperDefinition(
                                    function = function,
                                    converters = allConverters,
                                    mapperAnnotation = annotation,
                                )
                            }
                    }
                    .groupBy { definition -> definition.source }

                mappers.forEach { (sourceClass, definitions) ->
                    generateMapper(
                        module = module,
                        mappers = definitions,
                        sourceClass = sourceClass,
                    )
                }
            }

        logger.info(message = "AutoMapperProcessor: Round processing finished successfully")

        invalidSymbols
    }.getOrElse { throwable ->
        logger.error(message = "AutoMapperProcessor: Critical error during processing: ${throwable.message}")
        throwable.printStackTrace()

        emptyList()
    }

    /**
     * Orchestrates the generation of a specific mapper file for a source class
     *
     * Creates a file named `*Mapper.kt` containing all defined mapping
     * where [sourceClass] is the input. Handles dependency tracking for KSP incremental builds
     */
    private fun generateMapper(
        module: KSClassDeclaration,
        sourceClass: KSClassDeclaration,
        mappers: List<MapperDefinition>,
    ) {
        val fileName = "${sourceClass.toClassName().toJoinedSimpleName()}Mapper"
        val packageName = module.containingFile?.packageName?.asString().orEmpty()

        logger.info(message = "AutoMapperProcessor: Generating mapper file '$fileName' for source class '${sourceClass.toClassName()}'")

        runCatching {
            val fileSpec = buildFileSpec(
                module = module,
                mappers = mappers,
                fileName = fileName,
                packageName = packageName,
                sourceClass = sourceClass,
            )

            val allConverters = mappers.flatMap(transform = MapperDefinition::converters)
            val dependencies = buildList {
                add(element = module.containingFile)
                add(element = sourceClass.containingFile)
                addAll(elements = mappers.map { definition -> definition.target.containingFile })
                addAll(elements = allConverters.map { converter -> converter.function.containingFile })
            }
                .filterNotNull()
                .distinct()
                .toTypedArray()

            fileSpec.writeTo(
                codeGenerator = codeGenerator,
                dependencies = Dependencies(aggregating = false, *dependencies)
            )

            logger.info(message = "AutoMapperProcessor: Successfully generated '$fileName'")
        }.onFailure { throwable ->
            logger.error(message = "AutoMapperProcessor: Failed to generate mapper file '$fileName': ${throwable.message}")
            throwable.printStackTrace()
        }
    }

    /**
     * Constructs the KotlinPoet [FileSpec] for the mapper file
     */
    private fun buildFileSpec(
        fileName: String,
        packageName: String,
        module: KSClassDeclaration,
        sourceClass: KSClassDeclaration,
        mappers: List<MapperDefinition>,
    ): FileSpec {
        val sourceClassName = sourceClass.toClassName()

        val fileSpecBuilder = FileSpec.builder(
            fileName = fileName,
            packageName = packageName,
        )
            .indent(indent = "    ")
            .addFileComment(
                format = buildString {
                    appendLine("Generated by AutoMapperProcessor")
                    appendLine()
                    appendLine("Source Module: ${module.qualifiedName?.asString()}")
                    appendLine()
                    appendLine("Mappings:")
                    mappers.forEach { definition ->
                        appendLine(" > ${definition.source.qualifiedName?.asString()} -> ${definition.target.qualifiedName?.asString()}")
                        if (definition.reversible) {
                            appendLine(" > ${definition.target.qualifiedName?.asString()} -> ${definition.source.qualifiedName?.asString()}")
                        }
                    }
                }
            )

        val visibilityModifier = KModifier.INTERNAL.takeIf {
            module.modifiers.contains(element = Modifier.INTERNAL)
        } ?: KModifier.PUBLIC

        mappers.forEach { definition ->
            val targetClassName = definition.target.toClassName()
            val builder = MapperBuilderFactory.getMapperBuilder(
                logger = logger,
                source = definition.source,
                target = definition.target,
                converters = definition.converters,
                defaultValues = definition.defaultValues,
                propertyMappings = definition.propertyMappings,
            )

            val sourceToTargetFunBuilder =
                FunSpec.builder(name = "as${targetClassName.toJoinedSimpleName()}")
                    .addModifiers(visibilityModifier)
                    .receiver(receiverType = sourceClassName)
                    .returns(returnType = targetClassName)
                    .addKdoc(format = "Converts [%T] to [%T]", sourceClassName, targetClassName)

            addRuntimeParameters(funSpecBuilder = sourceToTargetFunBuilder, definition = definition)

            sourceToTargetFunBuilder.addCode(
                codeBlock = builder.buildConversion(
                    from = definition.source,
                    to = definition.target,
                )
            )

            collectOptInAnnotations(
                definition = definition,
                source = definition.source,
                target = definition.target,
            )?.let { annotation ->
                sourceToTargetFunBuilder.addAnnotation(annotationSpec = annotation)
            }

            fileSpecBuilder.addFunction(funSpec = sourceToTargetFunBuilder.build())

            if (definition.reversible) {
                val reversedMappings = definition.propertyMappings.map { property ->
                    PropertyMapping(from = property.to, to = property.from)
                }

                val targetToSourceFunBuilder =
                    FunSpec.builder(name = "as${sourceClassName.toJoinedSimpleName()}")
                        .addModifiers(visibilityModifier)
                        .receiver(receiverType = targetClassName)
                        .returns(returnType = sourceClassName)
                        .addKdoc(format = "Converts [%T] to [%T]", targetClassName, sourceClassName)
                        .addCode(
                            codeBlock = MapperBuilderFactory.getMapperBuilder(
                                logger = logger,
                                source = definition.target,
                                target = definition.source,
                                defaultValues = emptyList(), // Default values are not reversed
                                converters = definition.converters,
                                propertyMappings = reversedMappings,
                            ).buildConversion(from = definition.target, to = definition.source)
                        )

                collectOptInAnnotations(
                    definition = definition,
                    source = definition.target,
                    target = definition.source,
                )?.let { annotation ->
                    targetToSourceFunBuilder.addAnnotation(annotationSpec = annotation)
                }

                fileSpecBuilder.addFunction(funSpec = targetToSourceFunBuilder.build())
            }
        }

        return fileSpecBuilder.build()
    }
}
