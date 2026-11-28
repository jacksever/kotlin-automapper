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
import io.github.jacksever.automapper.annotation.DefaultValueSource.PARAMETER
import io.github.jacksever.automapper.annotation.DefaultValueSource.PARAMETER_WITH_DEFAULT
import io.github.jacksever.automapper.annotation.PropertyMapping
import io.github.jacksever.automapper.sample.converter.InstantConverter
import io.github.jacksever.automapper.sample.converter.UuidConverter
import io.github.jacksever.automapper.sample.model.EscapedDefaultSource
import io.github.jacksever.automapper.sample.model.EscapedDefaultTarget
import io.github.jacksever.automapper.sample.model.EscapedEnumSource
import io.github.jacksever.automapper.sample.model.EscapedEnumTarget
import io.github.jacksever.automapper.sample.model.EscapedPropertySource
import io.github.jacksever.automapper.sample.model.EscapedPropertyTarget

@AutoMapperModule(converters = [InstantConverter::class, UuidConverter::class])
internal interface EscapedIdentifierTestMapperModule {

    @AutoMapper(
        reversible = false,
        propertyMappings = [PropertyMapping(from = "source value", to = "target value")],
        defaultValues = [
            DefaultValue(property = "when", value = "99", source = PARAMETER_WITH_DEFAULT),
            DefaultValue(property = "object", value = "fallback"),
            DefaultValue(property = "is", value = "123", source = PARAMETER_WITH_DEFAULT),
            DefaultValue(property = "as", value = "missing"),
            DefaultValue(property = "class", source = PARAMETER),
        ]
    )
    fun propertyMapper(from: EscapedPropertySource): EscapedPropertyTarget

    @AutoMapper(
        reversible = false,
        defaultValues = [
            DefaultValue(property = "object", value = "inline"),
            DefaultValue(property = "when", value = "when"),
        ]
    )
    fun defaultMapper(from: EscapedDefaultSource): EscapedDefaultTarget

    @AutoMapper(
        reversible = false,
        propertyMappings = [PropertyMapping(from = "class", to = "object")]
    )
    fun enumMapper(from: EscapedEnumSource): EscapedEnumTarget
}
