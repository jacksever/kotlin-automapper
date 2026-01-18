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

/**
 * A helper object that provides conversion expressions for mapping between different object types
 *
 * Used when mapping nested objects (e.g. `User.address` -> `UserEntity.address`).
 * It assumes an extension function `asTarget()` exists for the source type if they are custom classes
 */
internal object ObjectConverter {

    /**
     * Attempts to generate a recursive mapping call for object types
     */
    fun getConversion(sourceType: KSType, targetType: KSType): CodeBlock {
        val sourceDeclaration = sourceType.declaration
        val targetDeclaration = targetType.declaration

        if (sourceDeclaration is KSClassDeclaration && targetDeclaration is KSClassDeclaration) {
            val sourcePkg = sourceDeclaration.packageName.asString()
            val targetPkg = targetDeclaration.packageName.asString()

            if (!sourcePkg.startsWith(prefix = "kotlin") && !targetPkg.startsWith(prefix = "kotlin")) {
                return CodeBlock.of(".as${targetDeclaration.simpleName.asString()}()")
            }
        }

        return CodeBlock.of("")
    }
}
