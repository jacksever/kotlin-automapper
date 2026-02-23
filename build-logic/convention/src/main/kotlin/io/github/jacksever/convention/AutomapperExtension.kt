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

package io.github.jacksever.convention

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

/**
 * Root DSL extension for configuring the Kotlin AutoMapper Gradle plugin
 *
 * This extension serves as the main entry point for all AutoMapper-specific
 * configuration within a module
 */
abstract class AutomapperExtension @Inject constructor(objects: ObjectFactory) {

    /**
     * Metadata describing the published AutoMapper artifact
     */
    val artifact: AutomapperArtifactExtension = objects.newInstance<AutomapperArtifactExtension>()

    /**
     * Configures the published artifact metadata
     */
    fun artifact(action: Action<AutomapperArtifactExtension>) {
        action.execute(artifact)
    }
}
