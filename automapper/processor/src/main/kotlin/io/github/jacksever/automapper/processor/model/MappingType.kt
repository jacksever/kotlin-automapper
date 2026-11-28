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

package io.github.jacksever.automapper.processor.model

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import io.github.jacksever.automapper.processor.resolver.MappingTypeResolver

/**
 * Represents a mapping type with its concrete arguments and resolved members
 *
 * A declaration such as `Box<T>` is shared by `Box<String>` and `Box<Int>`, while their
 * property and constructor parameter types differ. All member lookups use this instance's
 * containing type so builders and collectors observe the same substitutions
 *
 * @property type full mapping type after expanding type aliases
 * @property resolver resolver owned by the current KSP round
 */
internal class MappingType(
    val type: KSType,
    private val resolver: MappingTypeResolver,
) {

    /**
     * Class declaration used to select a mapping strategy and track source dependencies
     */
    val declaration: KSClassDeclaration =
        requireNotNull(value = type.declaration as? KSClassDeclaration) {
            "Mapping type '$type' must be a class"
        }

    /**
     * Concrete type that can be used in a constructor call, without use-site projections
     */
    val constructorType: KSType by lazy {
        resolver.constructorType(type = type)
    }

    private val constructorParameterTypes: Map<String, KSType> by lazy {
        val constructor = declaration.primaryConstructor ?: return@lazy emptyMap()
        val signature = constructor.asMemberOf(containing = constructorType)

        check(value = !signature.isError) { "Cannot resolve constructor of '$type'" }

        constructor.parameters.zip(other = signature.parameterTypes)
            .associate { (parameter, resolvedType) ->
                val name = requireNotNull(value = parameter.name).asString()
                val resolved = requireNotNull(value = resolvedType)

                name to resolver.expand(type = resolved)
            }
    }

    /**
     * Resolves a declared or inherited property in the context of [type]
     *
     * @param property property belonging to this class or one of its supertypes
     * @return property type with class parameters replaced by this instance's arguments
     */
    fun propertyType(property: KSPropertyDeclaration): KSType =
        resolver.expand(type = property.asMemberOf(containing = type))

    /**
     * Resolves a primary constructor parameter using the same signature as constructor generation
     *
     * @param parameter primary constructor parameter
     * @return substituted parameter type
     */
    fun parameterType(parameter: KSValueParameter): KSType =
        requireNotNull(value = constructorParameterTypes[parameter.name?.asString()]) {
            "Cannot resolve constructor parameter '${parameter.name}' of '$type'"
        }
}
