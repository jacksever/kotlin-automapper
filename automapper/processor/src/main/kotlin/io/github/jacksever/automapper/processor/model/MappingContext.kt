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

import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import io.github.jacksever.automapper.processor.resolver.ConverterResolver
import io.github.jacksever.automapper.processor.resolver.MappingTypeResolver

/**
 * Holds resolution state for one generated mapping function
 *
 * Forward and reverse mappings receive separate contexts. Converter usage recorded while building
 * an expression is later consumed by the opt-in collector, including converters used in collections.
 * Contexts and their KSP symbols are never retained between processing rounds
 *
 * @property types shared resolver for the current round
 * @property typeParameters KotlinPoet scope for the generated function's type variables and bounds
 */
internal class MappingContext(
    val types: MappingTypeResolver,
    val typeParameters: TypeParameterResolver,
) {

    /** Resolves converter signatures before checking their applicability */
    val converterResolver = ConverterResolver(types = types)

    /** Declarations of converters actually used by this function */
    val usedConverters = linkedSetOf<ConverterDefinition>()
}
