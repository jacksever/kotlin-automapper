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
 * Defines a single mapping rule for a property with a different name
 *
 * @property from name of the property in the source class
 * @property to name of the property in the target class
 */
@Target()
@Retention(AnnotationRetention.SOURCE)
annotation class PropertyMapping(
    val from: String,
    val to: String,
)
