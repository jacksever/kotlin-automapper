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
import io.github.jacksever.automapper.sample.model.ExperimentalGenericMappingApi
import io.github.jacksever.automapper.sample.model.GenericConverterValue
import io.github.jacksever.automapper.sample.model.GenericTarget

internal object GenericTestConverter {

    @AutoConverter
    @ExperimentalGenericMappingApi
    @OptIn(ExperimentalGenericMappingApi::class)
    fun <T> convert(value: GenericConverterValue<T>): GenericTarget<T> =
        GenericTarget(value.payload)
}

/** Used for testing inference from a collection supertype */
internal object GenericSupertypeConverter {

    @AutoConverter
    fun <T> convert(values: Iterable<T>): String = values.first().toString()
}

/** Used for testing converter upper bounds */
internal object BoundedGenericConverter {

    @AutoConverter
    fun <T : Number> convert(value: T): String = "number:$value"
}

/** Used for testing inherited generic converter methods */
internal open class GenericConverterBase<T> {

    @AutoConverter
    fun convert(value: T): String = "inherited:$value"
}

/** Used for testing type substitution in a registered converter owner */
internal object InheritedGenericConverter : GenericConverterBase<Int>()

/** Used for testing priority of more specific converter signatures */
internal object AnyGenericConverter {

    @AutoConverter
    fun <T> convert(value: T): String = "any:$value"
}

/** Used for testing a concrete converter against generic candidates */
internal object ExactGenericTestConverter {

    @AutoConverter
    fun convert(value: Int): String = "exact:$value"
}

/** Used for testing repeated type parameter constraints */
internal object RepeatedGenericConverter {

    @AutoConverter
    fun <T> convert(value: Pair<T, T>): String = "same:$value"
}

/** Used for testing fallback when repeated parameters are incompatible */
internal object MixedGenericConverter {

    @AutoConverter
    fun convert(value: Pair<Int, String>): String = "mixed:${value.first}:${value.second}"
}

/** Used for testing inference from the expected result type */
internal object ReturnOnlyGenericConverter {

    @AutoConverter
    fun <T> convert(value: Int): List<T> = emptyList()
}

/** Used for testing converters that require a reified type argument */
internal object ReifiedGenericConverter {

    @AutoConverter
    inline fun <reified T> convert(value: T): String = T::class.simpleName.orEmpty()
}
