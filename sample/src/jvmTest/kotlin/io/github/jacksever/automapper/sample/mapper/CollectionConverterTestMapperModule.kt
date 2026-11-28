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
import io.github.jacksever.automapper.sample.converter.CollectionItemConverter
import io.github.jacksever.automapper.sample.converter.CollectionNumberConverter
import io.github.jacksever.automapper.sample.converter.LocalCollectionItemConverter
import io.github.jacksever.automapper.sample.converter.NullableCollectionItemConverter
import io.github.jacksever.automapper.sample.converter.SameTypeCollectionItemConverter
import io.github.jacksever.automapper.sample.converter.WholeCollectionConverter
import io.github.jacksever.automapper.sample.model.CollectionFallbackTarget
import io.github.jacksever.automapper.sample.model.ConverterListSource
import io.github.jacksever.automapper.sample.model.ConverterListTarget
import io.github.jacksever.automapper.sample.model.ConverterSetSource
import io.github.jacksever.automapper.sample.model.ConverterSetTarget
import io.github.jacksever.automapper.sample.model.LocalConverterListTarget
import io.github.jacksever.automapper.sample.model.NestedConverterListSource
import io.github.jacksever.automapper.sample.model.NestedConverterListTarget
import io.github.jacksever.automapper.sample.model.NullableCollectionConverterSource
import io.github.jacksever.automapper.sample.model.NullableCollectionConverterTarget
import io.github.jacksever.automapper.sample.model.NullableConverterListSource
import io.github.jacksever.automapper.sample.model.NullableConverterListTarget
import io.github.jacksever.automapper.sample.model.NumberCollectionSource
import io.github.jacksever.automapper.sample.model.NumberCollectionTarget
import io.github.jacksever.automapper.sample.model.SameTypeCollectionTarget
import io.github.jacksever.automapper.sample.model.UnsafeConverterListTarget
import io.github.jacksever.automapper.sample.model.WholeConverterListTarget

@AutoMapperModule(converters = [CollectionItemConverter::class, CollectionNumberConverter::class])
internal interface CollectionConverterTestMapperModule {

    @AutoMapper
    fun listMapper(from: ConverterListSource): ConverterListTarget

    @AutoMapper(reversible = false)
    fun setTargetMapper(from: ConverterListSource): ConverterSetTarget

    @AutoMapper(reversible = false)
    fun setSourceMapper(from: ConverterSetSource): ConverterListTarget

    @AutoMapper(reversible = false)
    fun nestedMapper(from: NestedConverterListSource): NestedConverterListTarget

    @AutoMapper(reversible = false, converters = [LocalCollectionItemConverter::class])
    fun localMapper(from: ConverterListSource): LocalConverterListTarget

    @AutoMapper(reversible = false, converters = [WholeCollectionConverter::class])
    fun wholeMapper(from: ConverterListSource): WholeConverterListTarget

    @AutoMapper(reversible = false, converters = [SameTypeCollectionItemConverter::class])
    fun sameTypeMapper(from: ConverterListSource): SameTypeCollectionTarget

    @AutoMapper(reversible = false, converters = [NullableCollectionItemConverter::class])
    fun nullableInputMapper(from: NullableConverterListSource): ConverterListTarget

    @AutoMapper(reversible = false)
    fun nullableOutputMapper(from: ConverterListSource): NullableConverterListTarget

    @AutoMapper(reversible = false)
    fun unsafeMapper(from: NullableConverterListSource): UnsafeConverterListTarget

    @AutoMapper(reversible = false)
    fun nullableCollectionMapper(from: NullableCollectionConverterSource): NullableCollectionConverterTarget

    @AutoMapper(
        reversible = false,
        defaultValues = [DefaultValue(property = "items", source = DefaultValueSource.PARAMETER)]
    )
    fun fallbackMapper(from: NullableCollectionConverterSource): CollectionFallbackTarget

    @AutoMapper(reversible = false)
    fun numberMapper(from: NumberCollectionSource): NumberCollectionTarget
}
