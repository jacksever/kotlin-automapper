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

package io.github.jacksever.automapper.processor.extenstion

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * Converts this [KSFunctionDeclaration] into a KotlinPoet [MemberName]
 *
 * If the function is declared inside a class or object, the resulting [MemberName]
 * is created with an enclosing class reference. Otherwise, the function is treated
 * as a top-level declaration and the package name is used
 *
 * @return a [MemberName] that correctly represents how this function should be
 * referenced in generated Kotlin code
 */
internal fun KSFunctionDeclaration.asMemberName(): MemberName =
    when (val parent = parentDeclaration) {
        is KSClassDeclaration -> MemberName(
            simpleName = simpleName.asString(),
            enclosingClassName = parent.toClassName(),
        )

        else -> MemberName(
            simpleName = simpleName.asString(),
            packageName = packageName.asString(),
        )
    }
