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

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Safely retrieves the value of an annotation argument by its [name]
 */
internal fun KSAnnotation.getArgument(name: String) =
    arguments.firstOrNull { arg -> arg.name?.asString() == name }?.value

/**
 * Safely retrieves a list of nested annotations from an argument by its [name]
 */
internal fun KSAnnotation.getAnnotations(name: String) = (getArgument(name = name) as? List<*>)
    ?.filterIsInstance<KSAnnotation>()
    ?: emptyList()

/**
 * Retrieves a string-valued annotation argument by its [name]
 *
 * If the argument is not present or is not a string, the provided [default]
 * value is returned instead
 */
internal fun KSAnnotation.stringArg(name: String, default: String = ""): String =
    getArgument(name = name) as? String ?: default

/**
 * Retrieves a boolean-valued annotation argument by its [name]
 *
 * If the argument is not present or is not a boolean, the provided [default]
 * value is returned
 */
internal fun KSAnnotation.booleanArg(name: String, default: Boolean = false): Boolean =
    getArgument(name = name) as? Boolean ?: default

/**
 * Retrieves an enum-valued annotation argument by its [name]
 *
 * If the argument is not present or cannot be resolved, the provided [default]
 * value is returned
 */
internal inline fun <reified E : Enum<E>> KSAnnotation.enumArg(
    name: String,
    default: E,
): E = (getArgument(name = name) as? KSClassDeclaration)
    ?.simpleName
    ?.asString()
    ?.let { name -> enumValueOf<E>(name = name) }
    ?: default
