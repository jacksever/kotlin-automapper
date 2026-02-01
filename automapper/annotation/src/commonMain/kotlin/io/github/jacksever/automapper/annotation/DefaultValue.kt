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

package io.github.jacksever.automapper.annotation

/**
 * Specifies how a default value is provided for a target property
 */
enum class DefaultValueSource {

    /**
     * The value is inlined directly into the generated code
     */
    INLINE,

    /**
     * The value is provided as a required parameter to the generated mapping function
     */
    PARAMETER,

    /**
     * The value is provided as an optional parameter with a default fallback
     */
    PARAMETER_WITH_DEFAULT,
    ;
}

/**
 * Provides a default value for a property in the target class
 *
 * This is used within the `@AutoMapper` annotation to provide fallback values for properties
 * that cannot be mapped from the source object or are null
 *
 * @property property the name of the property in the target class
 * @property value the string representation of the default value. Required for INLINE and PARAMETER_WITH_DEFAULT modes
 * @property source the source determining how the default value is resolved
 */
@Target
@Retention(AnnotationRetention.SOURCE)
annotation class DefaultValue(
    val property: String,
    val value: String = "",
    val source: DefaultValueSource = DefaultValueSource.INLINE,
)
