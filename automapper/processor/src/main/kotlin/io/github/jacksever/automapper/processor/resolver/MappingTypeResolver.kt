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

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Variance
import io.github.jacksever.automapper.processor.model.MappingType

/**
 * Resolves type instances, substitutions and sealed subclass arguments for one KSP round
 *
 * Builders operate on [MappingType] rather than raw class declarations. This resolver is the shared
 * boundary for expanding aliases and applying substitutions, so property conversion, constructor
 * generation and annotation collection use consistent types
 *
 * @property resolver KSP resolver from the current processing round
 */
internal class MappingTypeResolver(private val resolver: Resolver) {

    /**
     * Creates a mapping type while preserving arguments and expanding type aliases
     *
     * @param type source or target type obtained from a mapper declaration
     */
    fun resolve(type: KSType): MappingType = MappingType(
        type = expand(type = type),
        resolver = this,
    )

    /** Creates a type argument through the current KSP resolver */
    fun argument(type: KSType, variance: Variance = Variance.INVARIANT): KSTypeArgument =
        resolver.getTypeArgument(
            typeRef = resolver.createKSTypeReferenceFromKSType(type = type),
            variance = variance,
        )

    /**
     * Finds the class visible through a source type or its type parameter's upper bounds
     *
     * This lookup is used for reading bounded generic properties. It does not imply that an
     * arbitrary type parameter can be instantiated as a mapping target
     */
    fun classDeclaration(type: KSType): KSClassDeclaration? {
        val visited = mutableSetOf<KSTypeParameter>()

        fun visit(current: KSType): KSClassDeclaration? =
            when (val declaration = current.declaration) {
                is KSClassDeclaration -> declaration
                is KSTypeParameter -> if (visited.add(element = declaration)) {
                    declaration.bounds.firstNotNullOfOrNull { bound -> visit(current = expand(type = bound.resolve())) }
                } else {
                    null
                }

                else -> null
            }

        return visit(current = expand(type = type))
    }

    /**
     * Expands aliases recursively, including aliases inside collection and model arguments
     *
     * @param type type to normalize
     * @return underlying type with its nullability and use-site variance preserved
     */
    fun expand(type: KSType): KSType {
        check(value = !type.isError) { "Cannot resolve type '$type'" }

        val alias = type.declaration as? KSTypeAlias
        if (alias != null) {
            val bindings = alias.typeParameters.zip(other = type.arguments).toMap()
            val expanded = substitute(type = alias.type.resolve(), bindings = bindings)

            return expand(type = if (type.isMarkedNullable) expanded.makeNullable() else expanded)
        }

        if (type.arguments.isEmpty()) return type

        val arguments = type.arguments.map { typeArgument ->
            typeArgument.type?.resolve()?.let { argumentType ->
                argument(type = expand(type = argumentType), variance = typeArgument.variance)
            } ?: typeArgument
        }

        return type.replace(arguments = arguments)
    }

    /**
     * Substitutes parameters by declaration identity rather than their potentially repeated names
     *
     * @param type type containing parameters to replace
     * @param bindings arguments inferred for the corresponding parameter declarations
     * @return substituted type with explicit nullable occurrences preserved
     */
    fun substitute(type: KSType, bindings: Map<KSTypeParameter, KSTypeArgument>): KSType {
        val replacement = (type.declaration as? KSTypeParameter)?.let(block = bindings::get)

        if (replacement != null) {
            val result = replacement.type?.resolve() ?: resolver.builtIns.anyType.makeNullable()

            return if (type.isMarkedNullable) result.makeNullable() else result
        }

        val arguments = type.arguments.map { typeArgument ->
            val argumentType = typeArgument.type?.resolve()
            val direct = (argumentType?.declaration as? KSTypeParameter)?.let(block = bindings::get)

            when {
                direct?.variance == Variance.STAR -> direct
                argumentType != null -> argument(
                    type = substitute(type = argumentType, bindings = bindings),
                    variance = typeArgument.variance,
                )

                else -> typeArgument
            }
        }

        return type.replace(arguments = arguments)
    }

    /**
     * Produces invariant constructor arguments from the public type's projected view
     *
     * Star projections are instantiated using the parameter's upper bound. The constructor signature
     * is then resolved in this concrete type instead of attempting to construct `Target<*>` directly
     *
     * @param type declared target type
     * @return a non-nullable type suitable for a constructor call
     */
    fun constructorType(type: KSType): KSType {
        val declaration = type.declaration as KSClassDeclaration
        val bindings = declaration.typeParameters.zip(other = type.arguments).toMap()
        val arguments = type.arguments.mapIndexed { index, typeArgument ->
            val concrete = typeArgument.type?.resolve()
                ?: declaration.typeParameters[index].bounds.firstOrNull()?.resolve()?.let { bound ->
                    substitute(type = bound, bindings = bindings)
                }
                ?: resolver.builtIns.anyType.makeNullable()

            argument(type = expand(type = concrete))
        }

        val concreteBindings = declaration.typeParameters.zip(other = arguments).toMap()
        check(
            value = boundsSatisfied(
                bindings = concreteBindings,
                parameters = declaration.typeParameters,
            )
        ) {
            "Cannot construct type '$type' from its projections and upper bounds; use concrete target type arguments"
        }

        return type.makeNotNullable().replace(arguments = arguments)
    }

    /**
     * Infers parameter bindings by matching a generic pattern against an actual type
     *
     * Existing bindings are preserved. Callers validate the substituted signature and its bounds
     * afterward; matching a pattern alone does not establish type compatibility
     *
     * @param pattern type containing parameters to infer
     * @param actual type against which the pattern is matched
     * @param parameters parameters owned by the declaration being instantiated
     * @param bindings inferred bindings, updated in place
     */
    fun infer(
        actual: KSType,
        pattern: KSType,
        parameters: Set<KSTypeParameter>,
        bindings: MutableMap<KSTypeParameter, KSTypeArgument>,
    ) {
        val expandedPattern = expand(type = pattern)
        val expandedActual = expand(type = actual)
        val parameter = expandedPattern.declaration as? KSTypeParameter

        if (parameter != null && parameter in parameters && parameter !in bindings) {
            val value = if (expandedPattern.isMarkedNullable) {
                expandedActual.makeNotNullable()
            } else {
                expandedActual
            }

            bindings[parameter] = argument(type = value)
            return
        }

        val actualView = if (expandedPattern.declaration == expandedActual.declaration) {
            expandedActual
        } else {
            val patternDeclaration = expandedPattern.declaration as? KSClassDeclaration ?: return

            supertype(type = expandedActual, target = patternDeclaration) ?: return
        }

        expandedPattern.arguments.zip(other = actualView.arguments)
            .forEach { (left, right) ->
                val leftType = left.type?.resolve()
                val rightType = right.type?.resolve()

                if (leftType != null && rightType != null) {
                    infer(
                        pattern = leftType,
                        actual = rightType,
                        bindings = bindings,
                        parameters = parameters,
                    )
                }
            }
    }

    /**
     * Checks every inferred argument against all substituted upper bounds
     */
    fun boundsSatisfied(
        parameters: List<KSTypeParameter>,
        bindings: Map<KSTypeParameter, KSTypeArgument>,
    ): Boolean = parameters.all { parameter ->
        val actual = bindings[parameter]?.type?.resolve() ?: return@all false

        parameter.bounds.all { bound ->
            substitute(type = bound.resolve(), bindings = bindings).isAssignableFrom(that = actual)
        }
    }

    /**
     * Finds a supertype while substituting arguments at each inheritance step
     */
    private fun supertype(
        type: KSType,
        target: KSClassDeclaration,
        visited: Set<KSClassDeclaration> = emptySet(),
    ): KSType? {
        val parameter = type.declaration as? KSTypeParameter
        if (parameter != null) {
            return parameter.bounds.firstNotNullOfOrNull { bound ->
                supertype(type = expand(type = bound.resolve()), target = target, visited = visited)
            }
        }

        val declaration = type.declaration as? KSClassDeclaration ?: return null
        if (declaration == target) return type
        if (declaration in visited) return null

        val bindings = declaration.typeParameters.zip(other = type.arguments).toMap()

        return declaration.superTypes.firstNotNullOfOrNull { reference ->
            supertype(
                target = target,
                visited = visited + declaration,
                type = expand(type = substitute(type = reference.resolve(), bindings = bindings)),
            )
        }
    }

    /**
     * Infers a leaf's arguments from its view of the requested sealed root
     */
    private fun leaf(root: MappingType, declaration: KSClassDeclaration): MappingType? {
        val parameters = declaration.typeParameters
        val patterns =
            declaration.superTypes.map { reference -> expand(type = reference.resolve()) }
        val parent = patterns.firstNotNullOfOrNull { pattern ->
            supertype(
                type = pattern,
                target = root.declaration,
            )
        } ?: return null

        val bindings = linkedMapOf<KSTypeParameter, KSTypeArgument>()

        infer(
            pattern = parent,
            actual = root.type,
            bindings = bindings,
            parameters = parameters.toSet(),
        )

        check(value = parameters.all { parameter -> parameter in bindings }) {
            "Cannot infer type arguments for sealed subclass '${declaration.qualifiedName?.asString()}' from '${root.type}'"
        }

        if (!boundsSatisfied(parameters = parameters, bindings = bindings)) return null

        val result =
            declaration.asType(typeArguments = parameters.map { parameter -> bindings.getValue(key = parameter) })

        return resolve(type = result).takeIf { root.type.isAssignableFrom(that = result) }
    }

    /**
     * Resolves all concrete leaves in the context of the original sealed root
     *
     * Indirect inheritance and reordered leaf parameters follow the same substitution path.
     * Leaves incompatible with the requested root arguments are excluded
     *
     * @param root sealed type instance or an ordinary mapping type
     * @return compatible leaves, or [root] itself for a non-sealed declaration
     */
    fun leaves(root: MappingType): List<MappingType> =
        if (Modifier.SEALED in root.declaration.modifiers) {
            leafDeclarations(declaration = root.declaration).mapNotNull { declaration ->
                leaf(root = root, declaration = declaration)
            }
        } else {
            listOf(root)
        }

    /**
     * Returns nominal leaves without filtering by generic arguments
     *
     * Kotlin exhaustiveness checks can still require a branch for a nominal leaf whose fixed
     * arguments are incompatible with the receiver. Builders use this list to detect that case
     */
    fun leafDeclarations(declaration: KSClassDeclaration): List<KSClassDeclaration> =
        if (Modifier.SEALED in declaration.modifiers) {
            declaration.getSealedSubclasses()
                .flatMap { subclass -> leafDeclarations(declaration = subclass) }
                .toList()
        } else {
            listOf(declaration)
        }
}
