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
import io.github.jacksever.automapper.sample.converter.AnimalAssignmentConverter
import io.github.jacksever.automapper.sample.model.AnimalTarget
import io.github.jacksever.automapper.sample.model.ConvertedAnimalTarget
import io.github.jacksever.automapper.sample.model.DogSource
import io.github.jacksever.automapper.sample.model.NullableAnimalTarget
import io.github.jacksever.automapper.sample.model.NullableDogSource
import io.github.jacksever.automapper.sample.model.NullableObjectSource
import io.github.jacksever.automapper.sample.model.NullableObjectTarget
import io.github.jacksever.automapper.sample.model.ObjectFallbackTarget
import io.github.jacksever.automapper.sample.model.ObjectSource
import io.github.jacksever.automapper.sample.model.ObjectTarget

@AutoMapperModule
internal interface ObjectAssignmentTestMapperModule {

    @AutoMapper(reversible = false)
    fun requiredObjectMapper(from: NullableObjectSource): ObjectTarget

    @AutoMapper
    fun nullableObjectMapper(from: ObjectSource): NullableObjectTarget

    @AutoMapper(
        reversible = false,
        defaultValues = [
            DefaultValue(property = "child", source = DefaultValueSource.PARAMETER),
        ]
    )
    fun fallbackObjectMapper(from: NullableObjectSource): ObjectFallbackTarget

    @AutoMapper(reversible = false)
    fun animalMapper(from: DogSource): AnimalTarget

    @AutoMapper(reversible = false)
    fun requiredAnimalMapper(from: NullableDogSource): AnimalTarget

    @AutoMapper(reversible = false)
    fun nullableAnimalMapper(from: DogSource): NullableAnimalTarget

    @AutoMapper(reversible = false)
    fun optionalAnimalMapper(from: NullableDogSource): NullableAnimalTarget

    @AutoMapper(reversible = false, converters = [AnimalAssignmentConverter::class])
    fun convertedAnimalMapper(from: DogSource): ConvertedAnimalTarget
}
