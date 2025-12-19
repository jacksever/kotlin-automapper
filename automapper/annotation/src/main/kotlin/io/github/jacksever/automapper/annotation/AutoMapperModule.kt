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
 * Marks an interface as a module for AutoMapper definitions
 *
 * This annotation serves as the entry point for the AutoMapper processor. It should be applied
 * to an interface containing one or more methods annotated with [AutoMapper]
 *
 * The visibility of the generated mapper extensions will match the visibility of this interface.
 * If the interface is `internal`, the generated extensions will be `internal`
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoMapperModule
