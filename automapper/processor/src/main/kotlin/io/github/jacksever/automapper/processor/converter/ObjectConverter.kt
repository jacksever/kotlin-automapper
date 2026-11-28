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

package io.github.jacksever.automapper.processor.converter

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ksp.toClassName
import io.github.jacksever.automapper.processor.extension.isAssignableConsideringNullability
import io.github.jacksever.automapper.processor.extension.toJoinedSimpleName
import io.github.jacksever.automapper.processor.resolver.MappingTypeResolver

/**
 * A helper object that provides conversion expressions for mapping between different object types
 *
 * Used when mapping nested objects (e.g. `User.address` -> `UserEntity.address`).
 * It assumes an extension function `asTarget()` exists for the source type if they are custom classes
 */
internal object ObjectConverter {

    /**
     * Generates a recursive mapping call for custom object types
     */
    fun getConversion(
        sourceType: KSType,
        targetType: KSType,
        types: MappingTypeResolver,
    ): CodeBlock {
        // Nullability is handled by ParameterHelper; compatible types need no mapper call
        if (targetType.isAssignableConsideringNullability(from = sourceType)) return EMPTY

        val sourceDeclaration = types.classDeclaration(type = sourceType) ?: return EMPTY
        val targetDeclaration = targetType.declaration as? KSClassDeclaration ?: return EMPTY

        if (sourceDeclaration.isFrameworkType() || targetDeclaration.isFrameworkType()) {
            return EMPTY
        }

        return CodeBlock.of(".as${targetDeclaration.toClassName().toJoinedSimpleName()}()")
    }

    /**
     * An empty conversion expression
     *
     * Used as a default or fallback when no specific conversion is needed
     */
    private val EMPTY = CodeBlock.of("")

    /**
     * Checks if a given KSP type belongs to a common framework package (e.g., "kotlin")
     */
    private fun KSClassDeclaration.isFrameworkType(): Boolean =
        packageName.asString().startsWith(prefix = "kotlin")
}
