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

package io.github.jacksever.automapper.processor.extension

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier

/**
 * Checks if the [KSType] represents an enum class
 */
internal fun KSType.isEnum(): Boolean =
    (declaration as? KSClassDeclaration)?.classKind == ClassKind.ENUM_CLASS

/**
 * Checks if the [KSType] represents a sealed class
 */
internal fun KSType.isSealed(): Boolean = declaration.modifiers.contains(Modifier.SEALED)

/**
 * Checks if a KSType is assignable from another, considering nullability
 */
internal fun KSType.isAssignableConsideringNullability(from: KSType): Boolean =
    isAssignableFrom(that = from) ||
            (from.isMarkedNullable && !isMarkedNullable &&
                    isAssignableFrom(that = from.makeNotNullable()))
