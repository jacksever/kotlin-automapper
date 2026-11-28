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

import com.google.devtools.ksp.symbol.KSType

/**
 * Represents a declared converter instantiated for a particular property or collection element
 *
 * Keeping the declaration separate preserves its original generic constraints for specificity
 * comparisons, while null handling uses the substituted input and output types
 *
 * @property from substituted input type
 * @property to substituted output type
 * @property definition original converter declaration and registered owner
 */
internal data class ResolvedConverter(
    val from: KSType,
    val to: KSType,
    val definition: ConverterDefinition,
)
