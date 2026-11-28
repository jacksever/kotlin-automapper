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
import io.github.jacksever.automapper.sample.converter.ExperimentalFunctionConverter
import io.github.jacksever.automapper.sample.converter.ExperimentalOwnerConverter
import io.github.jacksever.automapper.sample.converter.ExperimentalPropertyConverter
import io.github.jacksever.automapper.sample.model.AliasedOptInSource
import io.github.jacksever.automapper.sample.model.AliasedOptInTarget
import io.github.jacksever.automapper.sample.model.AnnotationOrderSealedSource
import io.github.jacksever.automapper.sample.model.AnnotationOrderSealedTarget
import io.github.jacksever.automapper.sample.model.AnnotationOrderSource
import io.github.jacksever.automapper.sample.model.AnnotationOrderTarget
import io.github.jacksever.automapper.sample.model.ConverterOptInSource
import io.github.jacksever.automapper.sample.model.ConverterOptInTarget
import io.github.jacksever.automapper.sample.model.MultipleOptInSource
import io.github.jacksever.automapper.sample.model.MultipleOptInTarget
import io.github.jacksever.automapper.sample.model.OptInFirstSource
import io.github.jacksever.automapper.sample.model.OptInFirstTarget
import io.github.jacksever.automapper.sample.model.PropertyOptInSource
import io.github.jacksever.automapper.sample.model.PropertyOptInTarget

@AutoMapperModule(
    converters = [
        ExperimentalPropertyConverter::class,
        ExperimentalFunctionConverter::class,
        ExperimentalOwnerConverter::class,
    ]
)
internal interface OptInTestMapperModule {

    @AutoMapper
    fun annotationOrderMapper(from: AnnotationOrderSource): AnnotationOrderTarget

    @AutoMapper(reversible = false)
    fun optInFirstMapper(from: OptInFirstSource): OptInFirstTarget

    @AutoMapper(reversible = false)
    fun propertyMapper(from: PropertyOptInSource): PropertyOptInTarget

    @AutoMapper(reversible = false)
    fun sealedMapper(from: AnnotationOrderSealedSource): AnnotationOrderSealedTarget

    @AutoMapper(reversible = false)
    fun multipleMarkersMapper(from: MultipleOptInSource): MultipleOptInTarget

    @AutoMapper(reversible = false)
    fun aliasedMapper(from: AliasedOptInSource): AliasedOptInTarget

    @AutoMapper(reversible = false)
    fun converterMapper(from: ConverterOptInSource): ConverterOptInTarget
}
