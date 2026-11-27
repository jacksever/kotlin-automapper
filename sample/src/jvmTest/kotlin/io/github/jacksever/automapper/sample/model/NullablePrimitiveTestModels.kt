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

package io.github.jacksever.automapper.sample.model

/** Source for nullable primitive conversion tests. */
data class NullablePrimitiveSource(val id: Int?)

/** Target for conversion without a fallback. */
data class PrimitiveWithoutDefaultTarget(val id: Long)

/** Target for conversion with an inline fallback. */
data class PrimitiveInlineDefaultTarget(val id: Long)

/** Target for conversion with a required runtime fallback. */
data class PrimitiveRequiredDefaultTarget(val id: Long)

/** Target for conversion with an optional runtime fallback. */
data class PrimitiveOptionalDefaultTarget(val id: Long)
