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
import com.google.devtools.ksp.symbol.KSAnnotation
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
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> = runCatching {
        logger.info("AutoMapperProcessor: Starting processing round...")

        val symbols =
            resolver.getSymbolsWithAnnotation(checkNotNull(AutoMapperModule::class.qualifiedName))
        val (validSymbols, invalidSymbols) = symbols.partition { symbol -> symbol.validate() }

        logger.info("AutoMapperProcessor: Found ${symbols.toList().size} annotated symbols. Valid: ${validSymbols.size}, Invalid (deferred): ${invalidSymbols.size}")

        validSymbols
            .filterIsInstance<KSClassDeclaration>()
            .forEach { module ->
                logger.info("AutoMapperProcessor: Processing module '${module.simpleName.asString()}'")

                val mappers = module.declarations
                    .filterIsInstance<KSFunctionDeclaration>()
                    .mapNotNull { function ->
                        function.annotations
                            .firstOrNull { annotation ->
                                annotation.shortName.asString() == AutoMapper::class.simpleName
                            }
                            ?.let { annotation ->
                                processMapperFunction(
                                    function = function,
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

        logger.info("AutoMapperProcessor: Round processing finished successfully")

        invalidSymbols
    }.getOrElse { throwable ->
        logger.error("AutoMapperProcessor: Critical error during processing: ${throwable.message}")
        throwable.printStackTrace()

        emptyList()
    }

    /**
     * Parses a single `@AutoMapper` annotated function into a [MapperDefinition]
     *
     * @param function function declaration to process
     * @param mapperAnnotation specific `@AutoMapper` annotation instance
     * @return Valid [MapperDefinition] or null if validation fails
     */
    private fun processMapperFunction(
        function: KSFunctionDeclaration,
        mapperAnnotation: KSAnnotation,
    ): MapperDefinition? = runCatching {
        val parameters = function.parameters
        val functionName = function.simpleName.asString()
        val reversible = mapperAnnotation.arguments
            .firstOrNull { args -> args.name?.asString() == "reversible" }
            ?.value as? Boolean ?: true
        val mappings = mapperAnnotation.arguments
            .firstOrNull { args -> args.name?.asString() == "mappings" }
            ?.value as? List<*>

        val propertyMappings = mappings
            ?.mapNotNull { mapping -> mapping as? KSAnnotation }
            ?.map { annotation ->
                val from =
                    annotation.arguments.first { args -> args.name?.asString() == "from" }.value as String
                val to =
                    annotation.arguments.first { args -> args.name?.asString() == "to" }.value as String

                PropertyMapping(from = from, to = to)
            } ?: emptyList()

        check(value = parameters.size == 1) {
            "Function '$functionName' annotated with @AutoMapper must have exactly one parameter representing the source object"
        }

        val sourceParam = parameters.first()
        val sourceType = sourceParam.type.resolve()
        val targetType = requireNotNull(value = function.returnType?.resolve()) {
            "Function '$functionName' annotated with @AutoMapper must declare a return type representing the target object"
        }
        val sourceClass = requireNotNull(value = sourceType.declaration as? KSClassDeclaration) {
            "Source type '${sourceType}' in function '$functionName' must be a class"
        }
        val targetClass = requireNotNull(value = targetType.declaration as? KSClassDeclaration) {
            "Target type '${targetType}' in function '$functionName' must be a class"
        }

        MapperDefinition(
            source = sourceClass,
            target = targetClass,
            reversible = reversible,
            propertyMappings = propertyMappings,
        )
    }.onFailure { throwable ->
        logger.error(
            "AutoMapperProcessor: Failed to process mapper function '${function.simpleName.asString()}': ${throwable.message}",
            function
        )
    }.getOrNull()

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
        val fileName = "${sourceClass.toClassName().simpleName}Mapper"
        val packageName = module.containingFile?.packageName?.asString().orEmpty()

        logger.info("AutoMapperProcessor: Generating mapper file '$fileName' for source class '${sourceClass.toClassName()}'")

        runCatching {
            val fileSpec = buildFileSpec(
                module = module,
                mappers = mappers,
                fileName = fileName,
                packageName = packageName,
                sourceClass = sourceClass,
            )

            val dependencies = (listOf(
                module.containingFile,
                sourceClass.containingFile
            ) + mappers.map { definition -> definition.target.containingFile })
                .filterNotNull()
                .distinct()
                .toTypedArray()

            fileSpec.writeTo(
                codeGenerator = codeGenerator,
                dependencies = Dependencies(aggregating = false, *dependencies)
            )

            logger.info("AutoMapperProcessor: Successfully generated '$fileName'")
        }.onFailure { throwable ->
            logger.error("AutoMapperProcessor: Failed to generate mapper file '$fileName': ${throwable.message}")
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

            val sourceToTargetFunBuilder =
                FunSpec.builder(name = "as${targetClassName.simpleName}")
                    .addModifiers(visibilityModifier)
                    .receiver(receiverType = sourceClassName)
                    .returns(returnType = targetClassName)
                    .addKdoc(format = "Converts [%T] to [%T]", sourceClassName, targetClassName)
                    .addCode(
                        codeBlock = MapperBuilderFactory.getMapperBuilder(
                            logger = logger,
                            source = definition.source,
                            target = definition.target,
                            propertyMappings = definition.propertyMappings,
                        ).buildConversion(from = definition.source, to = definition.target)
                    )

            fileSpecBuilder.addFunction(funSpec = sourceToTargetFunBuilder.build())

            if (definition.reversible) {
                val reversedMappings = definition.propertyMappings.map { mapping ->
                    PropertyMapping(from = mapping.to, to = mapping.from)
                }
                val targetToSourceFunBuilder =
                    FunSpec.builder(name = "as${sourceClassName.simpleName}")
                        .addModifiers(visibilityModifier)
                        .receiver(receiverType = targetClassName)
                        .returns(returnType = sourceClassName)
                        .addKdoc(format = "Converts [%T] to [%T]", targetClassName, sourceClassName)
                        .addCode(
                            codeBlock = MapperBuilderFactory.getMapperBuilder(
                                logger = logger,
                                source = definition.target,
                                target = definition.source,
                                propertyMappings = reversedMappings,
                            ).buildConversion(from = definition.target, to = definition.source)
                        )

                fileSpecBuilder.addFunction(funSpec = targetToSourceFunBuilder.build())
            }
        }

        return fileSpecBuilder.build()
    }
}
