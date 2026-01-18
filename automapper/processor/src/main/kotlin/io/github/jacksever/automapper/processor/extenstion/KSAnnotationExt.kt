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

import com.google.devtools.ksp.symbol.KSAnnotation

/**
 * Safely retrieves the value of an annotation argument by its [name]
 */
internal fun KSAnnotation.getArgument(name: String) =
    arguments.firstOrNull { arg -> arg.name?.asString() == name }?.value

/**
 * Safely retrieves a list of nested annotations from an argument by its [name]
 */
internal fun KSAnnotation.getAnnotations(name: String) = (getArgument(name) as? List<*>)
    ?.filterIsInstance<KSAnnotation>()
    ?: emptyList()
