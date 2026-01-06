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

/** Used for simple 1-to-1 sealed mapping test */
sealed interface SimpleSealedSource {

    data object A : SimpleSealedSource

    data class B(val value: Int) : SimpleSealedSource
}

/** Used for simple 1-to-1 sealed mapping test */
sealed interface SimpleSealedTarget {

    data object A : SimpleSealedTarget

    data class B(val value: Int) : SimpleSealedTarget
}

/** Used for testing sealed mapping with leaf renaming and property renaming */
sealed interface ComplexSealedSource {

    data object Common : ComplexSealedSource

    data class LeafSource(val sourceName: String) : ComplexSealedSource
}

/** Used for testing sealed mapping with leaf renaming and property renaming */
sealed interface ComplexSealedTarget {

    data object Common : ComplexSealedTarget

    data class LeafTarget(val targetName: String) : ComplexSealedTarget
}
