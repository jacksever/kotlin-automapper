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

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import io.github.jacksever.automapper.processor.extension.isAssignableConsideringNullability
import io.github.jacksever.automapper.processor.model.ConverterDefinition
import io.github.jacksever.automapper.processor.model.ResolvedConverter

/**
 * Resolves a converter's function parameters before applicability and specificity checks
 *
 * Inference first uses the source argument, then the expected result for parameters not yet bound.
 * Nullable inputs are also considered through a non-nullable view, preserving the existing safe-call
 * behavior for converters whose bounds prohibit null
 *
 * @property types shared type resolver for the current KSP round
 */
internal class ConverterResolver(private val types: MappingTypeResolver) {

    /**
     * Instantiates a converter for a particular source and target pair
     *
     * @param converter declared converter, including its registered owner
     * @param from actual source type
     * @param to expected target type
     * @return resolved converter, or null when its signature or bounds are incompatible
     */
    fun resolve(converter: ConverterDefinition, from: KSType, to: KSType): ResolvedConverter? {
        val sourceType = types.expand(type = from)
        val targetType = types.expand(type = to)
        val fromPattern = types.expand(type = converter.from)
        val toPattern = types.expand(type = converter.to)
        val parameters = converter.function.typeParameters

        if (parameters.isEmpty()) {
            return ResolvedConverter(definition = converter, from = fromPattern, to = toPattern)
                .takeIf { resolved ->
                    compatible(to = targetType, from = sourceType, converter = resolved)
                }
        }

        val parameterSet = parameters.toSet()

        return listOf(sourceType, sourceType.makeNotNullable())
            .distinct()
            .firstNotNullOfOrNull { input ->
                val bindings = linkedMapOf<KSTypeParameter, KSTypeArgument>()

                types.infer(
                    actual = input,
                    bindings = bindings,
                    pattern = fromPattern,
                    parameters = parameterSet,
                )
                types.infer(
                    actual = targetType,
                    bindings = bindings,
                    pattern = toPattern,
                    parameters = parameterSet,
                )

                if (!types.boundsSatisfied(parameters = parameters, bindings = bindings)) {
                    return@firstNotNullOfOrNull null
                }

                val hasInvalidReifiedArgument = parameters.any { parameter ->
                    parameter.isReified && bindings[parameter]?.type?.resolve()
                        ?.let(block = ::isReifiable) != true
                }

                if (hasInvalidReifiedArgument) return@firstNotNullOfOrNull null

                ResolvedConverter(
                    definition = converter,
                    from = types.expand(
                        type = types.substitute(type = fromPattern, bindings = bindings)
                    ),
                    to = types.expand(
                        type = types.substitute(type = toPattern, bindings = bindings)
                    ),
                ).takeIf { resolved ->
                    compatible(
                        to = targetType,
                        from = sourceType,
                        converter = resolved,
                    )
                }
            }
    }

    /**
     * Compares generic declarations before substitution erases their differences
     *
     * A pattern is more specific when the other converter accepts it, but the reverse match fails.
     * This gives concrete converters priority over unconstrained templates and respects upper bounds
     */
    fun isMoreSpecific(first: ConverterDefinition, second: ConverterDefinition): Boolean =
        resolve(converter = second, from = first.from, to = first.to) != null &&
                resolve(converter = first, from = second.from, to = second.to) == null

    /**
     * Checks whether Kotlin can provide the runtime type required by a reified call
     *
     * A type parameter used directly must be reified. Ordinary class arguments are erased, so
     * `List<T>` is valid even when `T` is not reified. Arrays retain their runtime element type
     * and therefore require the same check for that element. `Nothing` cannot be reified
     */
    private fun isReifiable(type: KSType): Boolean {
        val parameter = type.declaration as? KSTypeParameter
        if (parameter != null) return parameter.isReified

        return when (type.declaration.qualifiedName?.asString()) {
            "kotlin.Nothing" -> false
            "kotlin.Array" -> type.arguments.all { argument ->
                argument.type?.resolve()?.let { elementType -> isReifiable(type = elementType) }
                    ?: true
            }

            else -> true
        }
    }

    /**
     * Checks assignability using the same nullability policy as ordinary property converters
     */
    private fun compatible(converter: ResolvedConverter, from: KSType, to: KSType): Boolean =
        converter.from.isAssignableConsideringNullability(from = from) &&
                to.isAssignableConsideringNullability(from = converter.to)
}
