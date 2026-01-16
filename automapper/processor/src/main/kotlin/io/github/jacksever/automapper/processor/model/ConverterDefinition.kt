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

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType

/**
 * Represents a custom converter function found in a module
 *
 * @property from source type of the conversion
 * @property to target type of the conversion
 * @property function declaration of the converter function itself
 * @property optInAnnotations list of OptIn annotations required by the converter
 */
internal data class ConverterDefinition(
    val from: KSType,
    val to: KSType,
    val function: KSFunctionDeclaration,
    val optInAnnotations: List<KSAnnotation> = emptyList(),
)
