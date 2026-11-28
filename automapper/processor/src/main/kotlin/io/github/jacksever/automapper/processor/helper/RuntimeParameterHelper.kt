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

package io.github.jacksever.automapper.processor.helper

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import io.github.jacksever.automapper.annotation.DefaultValueSource.INLINE
import io.github.jacksever.automapper.annotation.DefaultValueSource.PARAMETER_WITH_DEFAULT
import io.github.jacksever.automapper.processor.formatter.DefaultValueFormatter.format
import io.github.jacksever.automapper.processor.model.MapperDefinition
import io.github.jacksever.automapper.processor.model.MappingContext

/**
 * A helper that adds runtime parameters to a mapper function specification
 */
internal interface RuntimeParameterHelper {

    /**
     * Adds parameters to the function spec builder based on runtime default values
     *
     * @param funSpecBuilder the builder for the mapper function
     * @param definition the definition of the mapper
     * @param context generic scope used by the generated function
     */
    fun addRuntimeParameters(
        funSpecBuilder: FunSpec.Builder,
        definition: MapperDefinition,
        context: MappingContext,
    )
}

/**
 * This class handles the logic for adding parameters to the generated mapper function
 */
internal class RuntimeParameterHelperImpl(
    private val logger: KSPLogger,
) : RuntimeParameterHelper {

    override fun addRuntimeParameters(
        funSpecBuilder: FunSpec.Builder,
        definition: MapperDefinition,
        context: MappingContext,
    ) {
        val runtimeDefaults =
            definition.defaultValues.filter { default -> default.source != INLINE }

        if (runtimeDefaults.isEmpty()) return

        if (Modifier.DATA !in definition.target.declaration.modifiers) {
            logger.error(
                message = "Runtime default values are only supported for data classes",
                symbol = definition.target.declaration
            )
            return
        }

        val constructorParams = definition.target.declaration.primaryConstructor?.parameters
            ?.associateBy { parameter -> parameter.name?.asString() }
            ?: run {
                logger.error(
                    message = "Data class ${definition.target.declaration.simpleName.asString()} must have a primary constructor to use runtime default values",
                    symbol = definition.target.declaration
                )
                return
            }

        runtimeDefaults.forEach { defaultValue ->
            val propertyName = defaultValue.property
            val targetParameter = constructorParams[propertyName]

            if (targetParameter == null) {
                logger.error(
                    message = "DefaultValue specified for non-existent property '$propertyName' in ${definition.target.declaration.simpleName.asString()}",
                    symbol = definition.target.declaration
                )
                return@forEach
            }

            val resolvedType = definition.target.parameterType(parameter = targetParameter)
            val paramType = resolvedType.toTypeName(typeParamResolver = context.typeParameters)

            val parameter = ParameterSpec.builder(name = propertyName, type = paramType)
                .apply {
                    if (defaultValue.source == PARAMETER_WITH_DEFAULT) {
                        defaultValue(
                            codeBlock = format(
                                targetType = resolvedType,
                                defaultValue = defaultValue.value,
                            )
                        )
                    }
                }
                .build()

            funSpecBuilder.addParameter(parameterSpec = parameter)
        }
    }
}
