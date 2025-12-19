/*
 * Copyright (c) 2025 Alexander Gorodnikov
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

package io.github.jacksever.automapper.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Provider class for [AutoMapperProcessor]
 *
 * This class acts as the entry point for the KSP framework to instantiate the processor.
 * It is registered via `META-INF/services/com.google.devtools.ksp.processing.SymbolProcessorProvider`
 */
internal class AutoMapperProcessorProvider : SymbolProcessorProvider {

    /**
     * Creates a new instance of [AutoMapperProcessor]
     *
     * @param environment KSP environment containing tools like logger and code generator
     * @return New instance of [AutoMapperProcessor]
     */
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        AutoMapperProcessor(
            logger = environment.logger,
            codeGenerator = environment.codeGenerator,
        )
}
