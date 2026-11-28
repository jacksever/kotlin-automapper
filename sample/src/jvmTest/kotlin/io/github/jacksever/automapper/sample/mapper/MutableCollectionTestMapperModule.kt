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
import io.github.jacksever.automapper.sample.model.ConvertedMutableListTarget
import io.github.jacksever.automapper.sample.model.ConvertedMutableSetTarget
import io.github.jacksever.automapper.sample.model.MutableItemCollectionSource
import io.github.jacksever.automapper.sample.model.MutableAnimalListTarget
import io.github.jacksever.automapper.sample.model.MutableAnimalSetTarget
import io.github.jacksever.automapper.sample.model.MutableDogListSource
import io.github.jacksever.automapper.sample.model.MutableDogSetSource
import io.github.jacksever.automapper.sample.model.MutableItemListTarget
import io.github.jacksever.automapper.sample.model.MutableItemSetTarget
import io.github.jacksever.automapper.sample.model.MutableListFallbackTarget
import io.github.jacksever.automapper.sample.model.MutableListSource
import io.github.jacksever.automapper.sample.model.MutableListTarget
import io.github.jacksever.automapper.sample.model.MutableNullableElementListTarget
import io.github.jacksever.automapper.sample.model.MutableSetSource
import io.github.jacksever.automapper.sample.model.MutableSetTarget
import io.github.jacksever.automapper.sample.model.NestedMutableListSource
import io.github.jacksever.automapper.sample.model.NestedMutableListTarget
import io.github.jacksever.automapper.sample.model.NullableMutableListTarget
import io.github.jacksever.automapper.sample.model.NullableReadOnlyListSource
import io.github.jacksever.automapper.sample.model.ReadOnlyListSource
import io.github.jacksever.automapper.sample.model.ReadOnlySetSource

@AutoMapperModule
internal interface MutableCollectionTestMapperModule {

    @AutoMapper
    fun listMapper(from: ReadOnlyListSource): MutableListTarget

    @AutoMapper
    fun setMapper(from: ReadOnlySetSource): MutableSetTarget

    @AutoMapper(reversible = false)
    fun listToSetMapper(from: ReadOnlyListSource): MutableSetTarget

    @AutoMapper(reversible = false)
    fun setToListMapper(from: ReadOnlySetSource): MutableListTarget

    @AutoMapper(reversible = false)
    fun convertedListMapper(from: ReadOnlyListSource): ConvertedMutableListTarget

    @AutoMapper(reversible = false)
    fun convertedSetMapper(from: ReadOnlyListSource): ConvertedMutableSetTarget

    @AutoMapper(reversible = false)
    fun mutableListMapper(from: MutableListSource): ConvertedMutableListTarget

    @AutoMapper(reversible = false)
    fun mutableSetMapper(from: MutableSetSource): ConvertedMutableSetTarget

    @AutoMapper(reversible = false)
    fun exactListMapper(from: MutableListSource): MutableListTarget

    @AutoMapper(reversible = false)
    fun exactSetMapper(from: MutableSetSource): MutableSetTarget

    @AutoMapper(reversible = false, converters = [CollectionItemConverter::class])
    fun itemListMapper(from: MutableItemCollectionSource): MutableItemListTarget

    @AutoMapper(reversible = false, converters = [CollectionItemConverter::class])
    fun itemSetMapper(from: MutableItemCollectionSource): MutableItemSetTarget

    @AutoMapper(reversible = false)
    fun nestedMapper(from: NestedMutableListSource): NestedMutableListTarget

    @AutoMapper(reversible = false)
    fun nullableMapper(from: NullableReadOnlyListSource): NullableMutableListTarget

    @AutoMapper(reversible = false)
    fun requiredMapper(from: NullableReadOnlyListSource): MutableListTarget

    @AutoMapper(
        reversible = false,
        defaultValues = [DefaultValue(property = "items", source = DefaultValueSource.PARAMETER)]
    )
    fun fallbackMapper(from: NullableReadOnlyListSource): MutableListFallbackTarget

    @AutoMapper(reversible = false)
    fun animalListMapper(from: MutableDogListSource): MutableAnimalListTarget

    @AutoMapper(reversible = false)
    fun animalSetMapper(from: MutableDogSetSource): MutableAnimalSetTarget

    @AutoMapper(reversible = false)
    fun nullableElementMapper(from: MutableListSource): MutableNullableElementListTarget
}
