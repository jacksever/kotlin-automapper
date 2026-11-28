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

data class ConverterItem(val id: Int)

data class ConverterListSource(val items: List<ConverterItem>)

data class ConverterListTarget(val items: List<String>)

data class ConverterSetSource(val items: Set<ConverterItem>)

data class ConverterSetTarget(val items: Set<String>)

data class LocalConverterListTarget(val items: List<String>)

data class WholeConverterListTarget(val items: List<String>)

data class NullableConverterListSource(val items: List<ConverterItem?>)

data class NullableConverterListTarget(val items: List<String?>)

data class UnsafeConverterListTarget(val items: List<String>)

data class NullableCollectionConverterSource(val items: List<ConverterItem?>?)

data class NullableCollectionConverterTarget(val items: List<String?>?)

data class CollectionFallbackTarget(val items: List<String>)

data class NestedConverterListSource(val items: List<List<ConverterItem>>)

data class NestedConverterListTarget(val items: List<List<String>>)

data class NumberCollectionSource(val items: List<Int>)

data class NumberCollectionTarget(val items: List<Long>)

data class SameTypeCollectionTarget(val items: List<ConverterItem>)
