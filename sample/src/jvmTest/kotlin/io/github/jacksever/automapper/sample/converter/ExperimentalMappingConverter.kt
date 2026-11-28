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
import io.github.jacksever.automapper.sample.model.ExperimentalFunctionMappingApi
import io.github.jacksever.automapper.sample.model.ExperimentalMappingApi
import io.github.jacksever.automapper.sample.model.ExperimentalMappingValue
import io.github.jacksever.automapper.sample.model.ExperimentalOwnerMappingApi

internal object ExperimentalPropertyConverter {

    @AutoConverter
    @OptIn(ExperimentalMappingApi::class)
    fun toText(content: ExperimentalMappingValue): String = content.text
}

internal object ExperimentalFunctionConverter {

    @AutoConverter
    @ExperimentalFunctionMappingApi
    @Suppress("unused")
    @OptIn(ExperimentalFunctionMappingApi::class)
    fun toText(id: Int): String = "id:$id"
}

@Suppress("unused")
@OptIn(ExperimentalOwnerMappingApi::class)
internal object ExperimentalOwnerConverter {

    @AutoConverter
    @ExperimentalOwnerMappingApi
    fun toText(flag: Boolean): String = flag.toString()
}
