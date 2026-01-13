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
 * Provides a default value for a property in the target class when no corresponding
 * source property is available for mapping
 *
 * This is used within the `@AutoMapper` annotation to provide fallback values for properties
 * that cannot be mapped from the source object
 *
 * Example:
 * ```
 * @AutoMapper(
 *     defaultValues = [
 *         DefaultValue(property = "status", value = "DEFAULT")
 *     ]
 * )
 * fun sourceMapping(source: SourceClass): TargetClass
 * ```
 *
 * @property property name of the property in the target class
 * @property value string representation of the default value
 */
@Target
@Retention(AnnotationRetention.SOURCE)
annotation class DefaultValue(
    val property: String,
    val value: String,
)
