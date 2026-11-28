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

package io.github.jacksever.automapper.processor.builder

import com.squareup.kotlinpoet.CodeBlock
import io.github.jacksever.automapper.processor.model.MappingType

/**
 * Interface defining the contract for generating conversion code blocks
 *
 * Implementations of this interface provide specific strategies for mapping different types of classes
 * (e.g., Data classes, Enums, Sealed classes)
 */
internal interface MapperBuilder {

    /**
     * Generates the code block to convert an instance of [from] class to an instance of [to] class
     *
     * @param from source type with resolved arguments
     * @param to target type with resolved arguments
     * @return [CodeBlock] representing the conversion logic (usually a return statement)
     */
    fun buildConversion(from: MappingType, to: MappingType): CodeBlock
}
