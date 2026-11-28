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

package io.github.jacksever.automapper.sample.converter

import io.github.jacksever.automapper.annotation.AutoConverter
import io.github.jacksever.automapper.sample.model.ConverterItem

internal object CollectionItemConverter {

    @AutoConverter
    fun toText(item: ConverterItem): String? = if (item.id < 0) null else "item:${item.id}"

    @AutoConverter
    fun fromText(text: String): ConverterItem = ConverterItem(id = text.removePrefix("item:").toInt())
}

internal object NullableCollectionItemConverter {

    @AutoConverter
    fun toText(item: ConverterItem?): String = item?.let { "nullable:${it.id}" } ?: "missing"
}

internal object LocalCollectionItemConverter {

    @AutoConverter
    fun toText(item: ConverterItem): String? = "local:${item.id}"
}

internal object WholeCollectionConverter {

    @AutoConverter
    fun toTexts(items: List<ConverterItem>): List<String> = listOf("whole:${items.size}")
}

internal object SameTypeCollectionItemConverter {

    @AutoConverter
    fun convert(item: ConverterItem): ConverterItem = item.copy(id = item.id + 100)
}

internal object CollectionNumberConverter {

    @AutoConverter
    fun convert(number: Int): Long = number.toLong() + 100L
}
