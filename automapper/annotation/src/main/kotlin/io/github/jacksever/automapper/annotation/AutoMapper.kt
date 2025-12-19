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

package io.github.jacksever.automapper.annotation

/**
 * Marks a function as a mapping definition for the AutoMapper processor
 *
 * This annotation should be applied to a function inside an interface marked with [AutoMapperModule]
 *
 * @property reversible if `true` (default), the processor will also generate a reverse mapping function
 * (Target -> Source) in addition to the direct mapping (Source -> Target). If `false`, only the direct mapping is generated
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoMapper(val reversible: Boolean = true)
