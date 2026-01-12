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

/** Used for a simple sealed class mapping test */
sealed interface SimpleSealedSource {

    data object A : SimpleSealedSource

    data class B(val value: Int) : SimpleSealedSource
}

/** Used for a simple sealed class mapping test */
sealed interface SimpleSealedTarget {

    data object A : SimpleSealedTarget

    data class B(val value: Int) : SimpleSealedTarget
}

/** Used for a sealed class mapping test with leaf renaming */
sealed interface ComplexSealedSource {

    data class LeafSource(val sourceName: String) : ComplexSealedSource
}

/** Used for a sealed class mapping test with leaf renaming */
sealed interface ComplexSealedTarget {

    data class LeafTarget(val targetName: String) : ComplexSealedTarget
}

/** Used for testing default value for a property within a sealed class leaf */
sealed interface SealedWithDefaultSource {

    object A : SealedWithDefaultSource

    data class B(val id: Int, val description: String?) : SealedWithDefaultSource
}

/** Used for testing default value for a property within a sealed class leaf */
sealed interface SealedWithDefaultTarget {

    object A : SealedWithDefaultTarget

    data class B(val id: Int, val description: String) : SealedWithDefaultTarget
}

/** Used to test providing a default value for a sealed type property */
sealed interface TaskState {

    data object Default : TaskState

    data class InProgress(val percent: Int) : TaskState

    data object Completed : TaskState
}

/** A source class that does NOT have a state property */
data class SourceWithMissingState(val id: String)

/** The test will provide a default value for this property during mapping */
data class TargetWithDefaultState(val id: String, val state: TaskState)
