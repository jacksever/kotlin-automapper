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
 * Strategy for generating mapping code for data classes
 *
 * This builder generates a constructor call for the target class, passing arguments derived from
 * the source class properties. It now supports custom mappings for properties with different names
 */
internal class DataMapperBuilder(
    private val logger: KSPLogger,
    private val context: MappingContext,
    private val defaultValues: List<DefaultValue>,
    private val converters: List<ConverterDefinition>,
    private val propertyMappings: List<PropertyMapping>,
) : MapperBuilder {

    /**
     * Generates a constructor call for the target class, applying custom property mappings
     *
     * Example output:
     * ```
     * return TargetClass(
     *     prop1 = prop1,
     *     prop2 = prop2.toLong(),
     * )
     * ```
     */
    override fun buildConversion(from: MappingType, to: MappingType): CodeBlock =
        buildCodeBlock {
            val params = buildConstructorParameters(
                target = to,
                source = from,
                logger = logger,
                context = context,
                converters = converters,
                defaultValues = defaultValues,
                propertyMappings = propertyMappings,
            )

            add(
                "return %T(\n",
                to.constructorType.toTypeName(typeParamResolver = context.typeParameters)
            )
            indent()
            params
                .filter { parameter -> parameter.isNotEmpty() }
                .forEach { param ->
                    add(param)
                    add(",\n")
                }
            unindent()
            add(")")
        }
}
