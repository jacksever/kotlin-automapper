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

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toClassName
import io.github.jacksever.automapper.annotation.PropertyMapping
import io.github.jacksever.automapper.processor.helper.ParameterHelper.buildConstructorParameters

/**
 * Strategy for generating mapping code for data classes
 *
 * This builder generates a constructor call for the target class, passing arguments derived from
 * the source class properties. It now supports custom mappings for properties with different names
 */
internal class DataMapperBuilder(private val propertyMappings: List<PropertyMapping>) : MapperBuilder {

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
    override fun buildConversion(from: KSClassDeclaration, to: KSClassDeclaration): CodeBlock =
        buildCodeBlock {
            val customMappings = propertyMappings.associate { mapping -> mapping.to to mapping.from }
            val params = buildConstructorParameters(
                sourceClass = from,
                targetClass = to,
                customMappings = customMappings,
            )

            add("return %T(\n", to.toClassName())
            indent()
            params.forEach { param -> addStatement("$param,") }
            unindent()
            add(")")
        }
}
