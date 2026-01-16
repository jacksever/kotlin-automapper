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

import kotlin.reflect.KClass

/**
 * Marks a function within an `@AutoMapperModule` as a mapping definition
 *
 * The function signature must have one parameter (the source) and a return type (the target).
 * The processor will generate extension functions based on this signature
 *
 * Example of a simple mapper:
 * ```
 * @AutoMapper
 * fun userMapper(user: User): UserDto
 * ```
 *
 * Example with customizations:
 * ```
 * @AutoMapper(
 *     reversible = true,
 *     converters = [InstantConverter::class],
 *     defaultValues = [
 *          DefaultValue(property = "role", value = "GUEST")
 *     ],
 *     propertyMappings = [
 *         PropertyMapping(from = "firstName", to = "name")
 *     ],
 * )
 * fun userMapper(user: User): UserDto
 * ```
 *
 * @property reversible if `true`, the processor will also generate a mapping from target to source
 * @property converters list of classes containing custom converter functions for this specific mapping
 * @property defaultValues list of [DefaultValue] rules for providing values for unmapped properties
 * @property propertyMappings list of [PropertyMapping] rules for renaming properties or handling nullability
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoMapper(
    val reversible: Boolean = true,
    val converters: Array<KClass<*>> = [],
    val defaultValues: Array<DefaultValue> = [],
    val propertyMappings: Array<PropertyMapping> = [],
)
