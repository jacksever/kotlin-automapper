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

package io.github.jacksever.automapper.processor.resolver

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import io.github.jacksever.automapper.annotation.DefaultValueSource.INLINE
import io.github.jacksever.automapper.processor.exception.MappingException
import io.github.jacksever.automapper.processor.extension.toJoinedSimpleName
import io.github.jacksever.automapper.processor.model.MapperDefinition
import io.github.jacksever.automapper.processor.model.MappingFunction

/**
 * Validates generated Kotlin overloads and assigns JVM names for one mapper file
 *
 * Kotlin conflicts are checked before type erasure. Generic overloads that remain distinguishable
 * in Kotlin receive separate JVM names while keeping their public Kotlin extension names
 *
 * @property logger logger used to report conflicts against the mapper module
 */
internal class MappingSignatureResolver(private val logger: KSPLogger) {

    /**
     * Resolves names for mappings whose forward and reverse directions have already been expanded
     *
     * @param module module to which signature diagnostics are attached
     * @param definitions mappings in their intended output order
     * @return validated function descriptions in the same order
     * @throws MappingException when two mappings produce the same Kotlin signature
     */
    fun resolve(
        module: KSClassDeclaration,
        definitions: List<MapperDefinition>
    ): List<MappingFunction> {
        val functions = definitions.map { definition ->
            MappingFunction(
                definition = definition,
                name = "as${definition.target.declaration.toClassName().toJoinedSimpleName()}",
            )
        }

        validateSignatures(module = module, functions = functions)

        val overloadCounts = functions.groupingBy { function ->
            function.definition.source.declaration to function.name
        }.eachCount()

        return functions.map { function ->
            val definition = function.definition
            val overloadCount =
                overloadCounts.getValue(key = definition.source.declaration to function.name)
            val hasGenericTypes = definition.source.type.arguments.isNotEmpty() ||
                    definition.target.type.arguments.isNotEmpty() || definition.typeParameters.isNotEmpty()

            if (overloadCount > 1 && hasGenericTypes) {
                function.copy(jvmName = "${function.name}_${definition.name}")
            } else {
                function
            }
        }
    }

    /**
     * Rejects declarations that differ only by return type or type-variable names
     */
    private fun validateSignatures(module: KSClassDeclaration, functions: List<MappingFunction>) {
        val conflicting = functions.groupBy { function -> kotlinSignature(function = function) }
            .values.firstOrNull { overloads -> overloads.size > 1 }
            ?: return

        val methods = conflicting.joinToString { function -> function.definition.name }
        val message = "Conflicting generated mapping signature for mapper methods: $methods"

        logger.error(message = message, symbol = module)
        throw MappingException(message = message)
    }

    /**
     * Describes a Kotlin overload using canonical type-variable names and no return type
     *
     * Runtime parameter types and upper bounds participate in overload resolution. Their textual
     * forms are compared separately instead of encoding the entire signature in a single string
     */
    private fun kotlinSignature(function: MappingFunction): KotlinSignature {
        val definition = function.definition
        val scope = canonicalTypeParameters(definition = definition)
        val bounds = definition.typeParameters.map { parameter ->
            parameter.bounds
                .map { bound -> bound.toTypeName(typeParamResolver = scope).toString() }
                .sorted()
                .toList()
        }

        return KotlinSignature(
            name = function.name,
            typeParameterBounds = bounds,
            parameterTypes = runtimeParameterTypes(definition = definition, scope = scope),
            receiver = definition.source.type.toTypeName(typeParamResolver = scope).toString(),
        )
    }

    /**
     * Normalizes parameter names so `<T>` and `<R>` do not distinguish otherwise identical functions
     */
    private fun canonicalTypeParameters(definition: MapperDefinition): TypeParameterResolver {
        val variables = definition.typeParameters.mapIndexed { index, parameter ->
            parameter.name.asString() to TypeVariableName(name = "T$index")
        }.toMap()

        return object : TypeParameterResolver {

            override val parametersMap: Map<String, TypeVariableName> = variables

            override fun get(index: String): TypeVariableName = variables.getValue(key = index)
        }
    }

    /**
     * Resolves runtime defaults using the same target constructor types as parameter generation
     */
    private fun runtimeParameterTypes(
        definition: MapperDefinition,
        scope: TypeParameterResolver,
    ): List<String> {
        val constructorParameters =
            definition.target.declaration.primaryConstructor?.parameters.orEmpty()
                .associateBy { parameter -> parameter.name?.asString() }

        return definition.defaultValues
            .filter { default -> default.source != INLINE }
            .map { default ->
                constructorParameters[default.property]?.let { parameter ->
                    definition.target.parameterType(parameter = parameter)
                        .toTypeName(typeParamResolver = scope)
                        .toString()
                } ?: "<missing:${default.property}>"
            }
    }

    /**
     * Components that distinguish Kotlin overloads after normalizing type-variable names
     */
    private data class KotlinSignature(
        val name: String,
        val receiver: String,
        val parameterTypes: List<String>,
        val typeParameterBounds: List<List<String>>,
    )
}
