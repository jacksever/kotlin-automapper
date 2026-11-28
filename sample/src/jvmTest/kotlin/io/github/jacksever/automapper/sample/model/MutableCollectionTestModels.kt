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

data class ReadOnlyListSource(val items: List<Int>)

data class ReadOnlySetSource(val items: Set<Int>)

data class MutableListSource(val items: MutableList<Int>)

data class MutableSetSource(val items: MutableSet<Int>)

data class MutableListTarget(val items: MutableList<Int>)

data class MutableSetTarget(val items: MutableSet<Int>)

data class ConvertedMutableListTarget(val items: MutableList<Long>)

data class ConvertedMutableSetTarget(val items: MutableSet<Long>)

data class NullableReadOnlyListSource(val items: List<Int>?)

data class NullableMutableListTarget(val items: MutableList<Int>?)

data class MutableListFallbackTarget(val items: MutableList<Int>)

data class NestedMutableListSource(val items: List<List<Int>>)

data class NestedMutableListTarget(val items: MutableList<MutableList<Long>>)

data class MutableItemListTarget(val items: MutableList<String>)

data class MutableItemSetTarget(val items: MutableSet<String>)

data class MutableDogListSource(val items: MutableList<Dog>)

data class MutableAnimalListTarget(val items: MutableList<Animal>)

data class MutableDogSetSource(val items: MutableSet<Dog>)

data class MutableAnimalSetTarget(val items: MutableSet<Animal>)

data class MutableNullableElementListTarget(val items: MutableList<Int?>)

data class MutableItemCollectionSource(val items: List<ConverterItem>)
