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
import io.github.jacksever.automapper.sample.model.ComplexSealedSource
import io.github.jacksever.automapper.sample.model.ComplexSealedTarget
import io.github.jacksever.automapper.sample.model.SealedWithDefaultSource
import io.github.jacksever.automapper.sample.model.SealedWithDefaultTarget
import io.github.jacksever.automapper.sample.model.SimpleSealedSource
import io.github.jacksever.automapper.sample.model.SimpleSealedTarget
import io.github.jacksever.automapper.sample.model.SourceWithMissingState
import io.github.jacksever.automapper.sample.model.TargetWithDefaultState

@AutoMapperModule
internal interface SealedClassTestMapperModule {

    @AutoMapper
    fun simpleMapper(from: SimpleSealedSource): SimpleSealedTarget

    @AutoMapper(
        reversible = false,
        propertyMappings = [
            PropertyMapping(from = "LeafSource", to = "LeafTarget"),
            PropertyMapping(from = "sourceName", to = "targetName"),
        ]
    )
    fun complexMapper(from: ComplexSealedSource): ComplexSealedTarget

    @AutoMapper(
        propertyMappings = [
            PropertyMapping(from = "description", defaultValue = "Default Description")
        ]
    )
    fun sealedWithDefaultMapper(from: SealedWithDefaultSource): SealedWithDefaultTarget

    @AutoMapper(
        propertyMappings = [
            PropertyMapping(from = "state", defaultValue = "Default")
        ]
    )
    fun sealedStateDefaultValueMapper(from: SourceWithMissingState): TargetWithDefaultState
}
