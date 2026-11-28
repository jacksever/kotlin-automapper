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

@RequiresOptIn
annotation class ExperimentalMappingApi

@RequiresOptIn
annotation class ExperimentalFirstMappingApi

@RequiresOptIn
annotation class ExperimentalSecondMappingApi

@RequiresOptIn
annotation class ExperimentalFunctionMappingApi

@RequiresOptIn
annotation class ExperimentalOwnerMappingApi

@ExperimentalMappingApi
data class ExperimentalMappingValue(val text: String)

@ExperimentalFirstMappingApi
data class ExperimentalFirstMappingValue(val text: String)

@ExperimentalSecondMappingApi
data class ExperimentalSecondMappingValue(val text: String)

@Suppress("unused")
@OptIn(ExperimentalMappingApi::class)
data class AnnotationOrderSource(val content: ExperimentalMappingValue)

@Suppress("unused")
@OptIn(ExperimentalMappingApi::class)
data class AnnotationOrderTarget(val content: ExperimentalMappingValue)

@OptIn(ExperimentalMappingApi::class)
@Suppress("unused")
data class OptInFirstSource(val content: ExperimentalMappingValue)

@OptIn(ExperimentalMappingApi::class)
@Suppress("unused")
data class OptInFirstTarget(val content: ExperimentalMappingValue)

data class PropertyOptInSource(val id: Int) {

    @Suppress("unused")
    @OptIn(ExperimentalMappingApi::class)
    val content: ExperimentalMappingValue
        get() = ExperimentalMappingValue(text = "property:$id")
}

data class PropertyOptInTarget(val content: String)

sealed interface AnnotationOrderSealedSource {

    @Suppress("unused")
    @OptIn(ExperimentalMappingApi::class)
    data class Value(val content: ExperimentalMappingValue) : AnnotationOrderSealedSource
}

sealed interface AnnotationOrderSealedTarget {

    @Suppress("unused")
    @OptIn(ExperimentalMappingApi::class)
    data class Value(val content: ExperimentalMappingValue) : AnnotationOrderSealedTarget
}

@Suppress("unused")
@OptIn(ExperimentalFirstMappingApi::class, ExperimentalSecondMappingApi::class)
data class MultipleOptInSource(
    val first: ExperimentalFirstMappingValue,
    val second: ExperimentalSecondMappingValue,
)

@Suppress("unused")
@OptIn(ExperimentalFirstMappingApi::class, ExperimentalSecondMappingApi::class)
data class MultipleOptInTarget(
    val first: ExperimentalFirstMappingValue,
    val second: ExperimentalSecondMappingValue,
)

sealed interface ConverterOptInSource {
    data class Value(val id: Int, val flag: Boolean) : ConverterOptInSource
}

sealed interface ConverterOptInTarget {
    data class Value(val id: String, val flag: String) : ConverterOptInTarget
}
