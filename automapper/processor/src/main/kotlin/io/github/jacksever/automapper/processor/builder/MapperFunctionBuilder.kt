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
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import io.github.jacksever.automapper.processor.collector.OptInAnnotationCollector
import io.github.jacksever.automapper.processor.collector.OptInAnnotationCollectorImpl
import io.github.jacksever.automapper.processor.helper.RuntimeParameterHelper
import io.github.jacksever.automapper.processor.helper.RuntimeParameterHelperImpl
import io.github.jacksever.automapper.processor.model.MapperDefinition
import io.github.jacksever.automapper.processor.model.MappingContext
import io.github.jacksever.automapper.processor.model.MappingFunction
import io.github.jacksever.automapper.processor.resolver.MappingTypeResolver

/**
 * Builds one KotlinPoet function from a validated mapping direction
 *
 * Each function receives an independent context. Conversion generation records the converters
 * actually used before opt-in annotations are collected
 *
 * @property logger logger shared with conversion builders
 */
internal class MapperFunctionBuilder(private val logger: KSPLogger) :
    RuntimeParameterHelper by RuntimeParameterHelperImpl(logger = logger),
    OptInAnnotationCollector by OptInAnnotationCollectorImpl(logger = logger) {

    /**
     * Builds the declaration, runtime parameters, conversion body, and required opt-ins in order
     *
     * @param types shared type resolver for the current KSP round
     * @param mapping mapping direction and names resolved for the containing file
     * @param visibility visibility inherited from the mapper module
     */
    fun build(
        visibility: KModifier,
        mapping: MappingFunction,
        types: MappingTypeResolver,
    ): FunSpec {
        val definition = mapping.definition
        val context = MappingContext(
            types = types,
            typeParameters = definition.typeParameters.toTypeParameterResolver(),
        )
        val function =
            buildDeclaration(visibility = visibility, context = context, mapping = mapping)

        addRuntimeParameters(funSpecBuilder = function, definition = definition, context = context)
        function.addKdoc(
            block = buildKdoc(definition = definition, parameters = function.parameters)
        )

        val conversionBuilder = MapperBuilderFactory.getMapperBuilder(
            logger = logger,
            context = context,
            source = definition.source,
            target = definition.target,
            converters = definition.converters,
            defaultValues = definition.defaultValues,
            propertyMappings = definition.propertyMappings,
        )

        function.addCode(
            codeBlock = conversionBuilder.buildConversion(
                from = definition.source,
                to = definition.target,
            )
        )

        collectOptInAnnotations(
            context = context,
            source = definition.source,
            target = definition.target,
        )?.let { annotation ->
            function.addAnnotation(annotationSpec = annotation)
        }

        return function.build()
    }

    /**
     * Creates the generic extension declaration with its visibility and optional JVM name
     */
    private fun buildDeclaration(
        visibility: KModifier,
        context: MappingContext,
        mapping: MappingFunction,
    ): FunSpec.Builder {
        val definition = mapping.definition
        val typeVariables = definition.typeParameters.map { parameter ->
            TypeVariableName(
                name = parameter.name.asString(),
                bounds = parameter.bounds.map { reference ->
                    reference.toTypeName(typeParamResolver = context.typeParameters)
                }.toList(),
            ).copy(reified = parameter.isReified)
        }
        val function = FunSpec.builder(name = mapping.name)
            .addModifiers(visibility)
            .addTypeVariables(typeVariables = typeVariables)
            .receiver(receiverType = definition.source.type.toTypeName(typeParamResolver = context.typeParameters))
            .returns(returnType = definition.target.type.toTypeName(typeParamResolver = context.typeParameters))

        if (definition.typeParameters.any { parameter -> parameter.isReified }) {
            function.addModifiers(KModifier.INLINE)
        }

        val jvmName = mapping.jvmName
        if (jvmName != null) {
            val annotation = AnnotationSpec.builder(type = ClassName("kotlin.jvm", "JvmName"))
                .addMember(format = "%S", jvmName)
                .build()

            function.addAnnotation(annotationSpec = annotation)
        }

        return function
    }

    /**
     * Builds the summary, runtime parameter descriptions, and source link as one KDoc block
     *
     * Parameter tags precede the source link without blank lines between tags. Descriptions use
     * the parameters actually generated and respect renamed or missing source properties
     */
    private fun buildKdoc(
        definition: MapperDefinition,
        parameters: List<ParameterSpec>,
    ): CodeBlock = buildCodeBlock {
        add(
            "Maps this [%T] to [%T].\n",
            definition.source.declaration.toClassName(),
            definition.target.declaration.toClassName(),
        )

        val module = definition.function.parentDeclaration as? KSClassDeclaration
        if (parameters.isNotEmpty() || module != null) add("\n")

        if (parameters.isNotEmpty()) {
            val sourcePropertyNames = definition.source.declaration.getAllProperties()
                .map { property -> property.simpleName.asString() }
                .toSet()

            parameters.forEach { parameter ->
                val sourcePropertyName = definition.propertyMappings
                    .firstOrNull { mapping -> mapping.to == parameter.name }
                    ?.from ?: parameter.name
                val description = if (sourcePropertyName in sourcePropertyNames) {
                    "Fallback for `%L` if its mapped value is null and the target is non-nullable."
                } else {
                    "Value for `%L`, which has no matching source property."
                }

                add("@param %L ", parameter.name)
                add(description, parameter.name)
                add("\n")
            }
        }

        if (module != null) {
            add("@see %T.%N\n", module.toClassName(), definition.name)
        }
    }
}
