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
 * Defines a single mapping rule for a property with a different name or nullability handling
 *
 * @property from name of the property in the source class
 * @property to name of the property in the target class. If omitted, it is assumed to be the same as [from]
 * @property defaultValue string literal to be used as a default value if the source property is null and the target is not nullable.
 * For example, `defaultValue = "\"\""` for an empty string, or `defaultValue = "0L"` for a Long
 */
@Target
@Retention(AnnotationRetention.SOURCE)
annotation class PropertyMapping(
    val from: String,
    val to: String = "",
    val defaultValue: String = "",
)
