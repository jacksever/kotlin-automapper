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
import io.github.jacksever.automapper.annotation.PropertyMapping
import io.github.jacksever.automapper.sample.model.EnumDefaultValueSource
import io.github.jacksever.automapper.sample.model.EnumDefaultValueTarget
import io.github.jacksever.automapper.sample.model.RenameEnumSource
import io.github.jacksever.automapper.sample.model.RenameEnumTarget
import io.github.jacksever.automapper.sample.model.SimpleEnumSource
import io.github.jacksever.automapper.sample.model.SimpleEnumTarget
import io.github.jacksever.automapper.sample.model.UnmappedEnumSource
import io.github.jacksever.automapper.sample.model.UnmappedEnumTarget

@AutoMapperModule
internal interface EnumTestMapperModule {

    @AutoMapper
    fun simpleMapper(from: SimpleEnumSource): SimpleEnumTarget

    @AutoMapper(reversible = false)
    fun unmappedMapper(from: UnmappedEnumSource): UnmappedEnumTarget

    @AutoMapper(
        reversible = false,
        propertyMappings = [
            PropertyMapping(from = "USER_ACTIVE", to = "ACTIVE"),
            PropertyMapping(from = "PENDING_APPROVAL", to = "PENDING"),
        ]
    )
    fun renameMapper(from: RenameEnumSource): RenameEnumTarget

    @AutoMapper(
        propertyMappings = [
            PropertyMapping(from = "priority", defaultValue = "HIGH")
        ]
    )
    fun enumDefaultValueMapper(from: EnumDefaultValueSource): EnumDefaultValueTarget
}
