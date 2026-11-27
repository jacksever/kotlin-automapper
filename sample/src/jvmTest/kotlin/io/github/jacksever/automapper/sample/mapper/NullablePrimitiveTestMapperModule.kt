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

package io.github.jacksever.automapper.sample.mapper

import io.github.jacksever.automapper.annotation.AutoMapper
import io.github.jacksever.automapper.annotation.AutoMapperModule
import io.github.jacksever.automapper.annotation.DefaultValue
import io.github.jacksever.automapper.annotation.DefaultValueSource
import io.github.jacksever.automapper.sample.model.NullablePrimitiveSource
import io.github.jacksever.automapper.sample.model.PrimitiveInlineDefaultTarget
import io.github.jacksever.automapper.sample.model.PrimitiveOptionalDefaultTarget
import io.github.jacksever.automapper.sample.model.PrimitiveRequiredDefaultTarget
import io.github.jacksever.automapper.sample.model.PrimitiveWithoutDefaultTarget

@AutoMapperModule
internal interface NullablePrimitiveTestMapperModule {

    @AutoMapper(reversible = false)
    fun withoutDefaultMapper(from: NullablePrimitiveSource): PrimitiveWithoutDefaultTarget

    @AutoMapper(
        reversible = false,
        defaultValues = [DefaultValue(property = "id", value = "0L")],
    )
    fun inlineDefaultMapper(from: NullablePrimitiveSource): PrimitiveInlineDefaultTarget

    @AutoMapper(
        reversible = false,
        defaultValues = [
            DefaultValue(property = "id", source = DefaultValueSource.PARAMETER),
        ],
    )
    fun requiredDefaultMapper(from: NullablePrimitiveSource): PrimitiveRequiredDefaultTarget

    @AutoMapper(
        reversible = false,
        defaultValues = [
            DefaultValue(
                property = "id",
                value = "0L",
                source = DefaultValueSource.PARAMETER_WITH_DEFAULT,
            ),
        ],
    )
    fun optionalDefaultMapper(from: NullablePrimitiveSource): PrimitiveOptionalDefaultTarget
}
