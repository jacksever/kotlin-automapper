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
import io.github.jacksever.automapper.annotation.PropertyMapping
import io.github.jacksever.automapper.sample.converter.GlobalPriorityConverter
import io.github.jacksever.automapper.sample.converter.InstantConverter
import io.github.jacksever.automapper.sample.converter.LocalPriorityConverter
import io.github.jacksever.automapper.sample.model.ComplexSource
import io.github.jacksever.automapper.sample.model.ComplexTarget
import io.github.jacksever.automapper.sample.model.DefaultValueSource
import io.github.jacksever.automapper.sample.model.DefaultValueTarget
import io.github.jacksever.automapper.sample.model.InstantConverterSource
import io.github.jacksever.automapper.sample.model.InstantConverterTarget
import io.github.jacksever.automapper.sample.model.NullabilitySource
import io.github.jacksever.automapper.sample.model.NullabilityTarget
import io.github.jacksever.automapper.sample.model.PrioritySource
import io.github.jacksever.automapper.sample.model.PriorityTarget
import io.github.jacksever.automapper.sample.model.RenameSource
import io.github.jacksever.automapper.sample.model.RenameTarget
import io.github.jacksever.automapper.sample.model.ReversibleSource
import io.github.jacksever.automapper.sample.model.ReversibleTarget
import io.github.jacksever.automapper.sample.model.SimpleSource
import io.github.jacksever.automapper.sample.model.SimpleTarget
import io.github.jacksever.automapper.sample.model.TypeConversionSource
import io.github.jacksever.automapper.sample.model.TypeConversionTarget
import io.github.jacksever.automapper.sample.model.UnsafeSource
import io.github.jacksever.automapper.sample.model.UnsafeTarget

@AutoMapperModule(converters = [GlobalPriorityConverter::class])
internal interface DataClassTestMapperModule {

    @AutoMapper
    fun simpleMapper(from: SimpleSource): SimpleTarget

    @AutoMapper
    fun unsafeMapper(from: UnsafeSource): UnsafeTarget

    @AutoMapper(converters = [LocalPriorityConverter::class])
    fun priorityMapper(from: PrioritySource): PriorityTarget

    @AutoMapper
    fun nullabilityMapper(from: NullabilitySource): NullabilityTarget

    @AutoMapper
    fun typeConversionMapper(from: TypeConversionSource): TypeConversionTarget

    @AutoMapper(converters = [InstantConverter::class])
    fun customTypeConverterMapper(from: InstantConverterSource): InstantConverterTarget

    @AutoMapper(
        propertyMappings = [
            PropertyMapping(from = "sourceId", to = "targetId"),
            PropertyMapping(from = "sourceName", to = "targetName"),
        ]
    )
    fun renameMapper(from: RenameSource): RenameTarget

    @AutoMapper(
        defaultValues = [
            DefaultValue(property = "content", value = "Empty"),
        ],
        propertyMappings = [
            PropertyMapping(from = "legacyId", to = "id"),
            PropertyMapping(from = "statusNum", to = "statusText"),
        ]
    )
    fun complexMapper(from: ComplexSource): ComplexTarget

    @AutoMapper(
        reversible = true,
        propertyMappings = [
            PropertyMapping(from = "originalId", to = "mappedId"),
        ]
    )
    fun reversibleMapper(from: ReversibleSource): ReversibleTarget

    @AutoMapper(
        defaultValues = [
            DefaultValue(property = "description", value = "Default"),
        ]
    )
    fun defaultValueMapper(from: DefaultValueSource): DefaultValueTarget
}
