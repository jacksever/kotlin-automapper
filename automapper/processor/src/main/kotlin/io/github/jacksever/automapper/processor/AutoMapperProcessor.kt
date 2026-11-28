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

import com.google.devtools.ksp.ApiFeatures
import com.google.devtools.ksp.buildApiFeatures
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.toClassName
import io.github.jacksever.automapper.annotation.AutoMapper
import io.github.jacksever.automapper.annotation.AutoMapperModule
import io.github.jacksever.automapper.processor.builder.MapperFileBuilder
import io.github.jacksever.automapper.processor.builder.MapperFileBuilderImpl
import io.github.jacksever.automapper.processor.collector.ConverterCollector
import io.github.jacksever.automapper.processor.collector.ConverterCollectorImpl
import io.github.jacksever.automapper.processor.collector.MapperDefinitionCollector
import io.github.jacksever.automapper.processor.collector.MapperDefinitionCollectorImpl
import io.github.jacksever.automapper.processor.exception.MappingException
import io.github.jacksever.automapper.processor.extension.toJoinedSimpleName
import io.github.jacksever.automapper.processor.model.MapperDefinition
import io.github.jacksever.automapper.processor.resolver.MappingTypeResolver

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
    MapperFileBuilder by MapperFileBuilderImpl(logger = logger),
    ConverterCollector by ConverterCollectorImpl(logger = logger),
    MapperDefinitionCollector by MapperDefinitionCollectorImpl(logger = logger) {

    override val apiFeatures: ApiFeatures = buildApiFeatures {
        enableBackingFields = true
        enableContextParameters = true
    }

    override fun process(resolver: Resolver): List<KSAnnotated> = runCatching {
        logger.info(message = "AutoMapperProcessor: Starting processing round...")

        val types = MappingTypeResolver(resolver = resolver)
        val symbols =
            resolver.getSymbolsWithAnnotation(checkNotNull(AutoMapperModule::class.qualifiedName))
        val (validSymbols, invalidSymbols) = symbols.partition { symbol ->
            symbol.validate(apiFeatures = apiFeatures)
        }

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
                                    .groupBy { converter -> converter.from to converter.to }
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
                                    types = types,
                                    function = function,
                                    converters = allConverters,
                                    mapperAnnotation = annotation,
                                )
                            }
                    }
                    .groupBy { definition -> definition.source.declaration }

                mappers.forEach { (sourceClass, definitions) ->
                    generateMapper(
                        types = types,
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
        types: MappingTypeResolver,
        module: KSClassDeclaration,
        sourceClass: KSClassDeclaration,
        mappers: List<MapperDefinition>,
    ) {
        val fileName = "${sourceClass.toClassName().toJoinedSimpleName()}Mapper"
        val packageName = module.containingFile?.packageName?.asString().orEmpty()

        logger.info(message = "AutoMapperProcessor: Generating mapper file '$fileName' for source class '${sourceClass.toClassName()}'")

        runCatching {
            val fileSpec = buildMapperFile(
                types = types,
                module = module,
                mappers = mappers,
                fileName = fileName,
                packageName = packageName,
            )

            val allConverters = mappers.flatMap(transform = MapperDefinition::converters)
            val dependencies = buildList {
                add(element = module.containingFile)
                add(element = sourceClass.containingFile)
                addAll(elements = mappers.map { definition -> definition.target.declaration.containingFile })
                addAll(elements = allConverters.map { converter -> converter.function.containingFile })
                addAll(elements = allConverters.map { converter -> converter.owner.containingFile })
            }
                .filterNotNull()
                .distinct()
                .toTypedArray()

            writeMapperFile(
                fileSpec = fileSpec,
                dependencies = Dependencies(aggregating = false, *dependencies),
            )

            logger.info(message = "AutoMapperProcessor: Successfully generated '$fileName'")
        }.onFailure { throwable ->
            when (throwable) {
                is MappingException -> {
                    // Don't rethrow, just log and skip file generation
                    logger.info(message = "AutoMapperProcessor: Aborted generation of '$fileName' due to a mapping error. See previous errors for details")
                }

                else -> {
                    logger.error(message = "AutoMapperProcessor: Failed to generate mapper file '$fileName': ${throwable.message}")
                    throwable.printStackTrace()
                }
            }
        }
    }

    /**
     * Writes the file header separately to leave an empty line before the Kotlin source
     *
     * KotlinPoet emits file comments immediately before the package declaration. Rendering the
     * header separately preserves a plain empty separator without adding another comment line
     */
    private fun writeMapperFile(fileSpec: FileSpec, dependencies: Dependencies) {
        val output = codeGenerator.createNewFile(
            fileName = fileSpec.name,
            dependencies = dependencies,
            packageName = fileSpec.packageName,
        )

        output.bufferedWriter().use { writer ->
            if (fileSpec.comment.isNotEmpty()) {
                fileSpec.comment.toString().lineSequence().forEach { line ->
                    writer.appendLine("// $line")
                }
                writer.appendLine()
            }

            fileSpec.toBuilder().clearComment().build().writeTo(out = writer)
        }
    }
}
